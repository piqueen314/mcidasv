/*
 * $Id$
 *
 * This file is part of McIDAS-V
 *
 * Copyright 2007-2011
 * Space Science and Engineering Center (SSEC)
 * University of Wisconsin - Madison
 * 1225 W. Dayton Street, Madison, WI 53706, USA
 * http://www.ssec.wisc.edu/mcidas
 * 
 * All Rights Reserved
 * 
 * McIDAS-V is built on Unidata's IDV and SSEC's VisAD libraries, and
 * some McIDAS-V source code is based on IDV and VisAD source code.  
 * 
 * McIDAS-V is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * McIDAS-V is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */
package edu.wisc.ssec.mcidasv.servermanager;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.bushe.swing.event.EventBus;

import edu.wisc.ssec.mcidasv.Constants;
import edu.wisc.ssec.mcidasv.McIDASV;

/**
 * Thread that actually execs mcservl
 */
public class AddeThread extends Thread {

    /** Mcserv events. */
    public enum McservEvent { 
        /** Mcservl is actively listening. */
        ACTIVE, 
        /** Mcservl has died unexpectedly. */
        DIED, 
        /** Mcservl started listening. */
        STARTED,
        /** Mcservl has stopped listening. */
        STOPPED 
    };

    /** */
    Process proc;

    /** Server manager. */
    private final EntryStore entryStore;

    /**
     * Creates a thread that controls a mcservl process.
     * 
     * @param entryStore Server manager.
     */
    public AddeThread(final EntryStore entryStore) {
        this.entryStore = entryStore;
    }

    public void run() {
        StringBuffer err = new StringBuffer();
        String[] cmds = entryStore.getAddeCommands();
        String[] env = (McIDASV.isWindows()) ? entryStore.getWindowsAddeEnv() : entryStore.getUnixAddeEnv();
        try {
            //start ADDE binary with "-p PORT" and set environment appropriately
            proc = Runtime.getRuntime().exec(cmds, env);

            //create thread for reading inputStream (process' stdout)
            StreamReaderThread outThread = new StreamReaderThread(proc.getInputStream(), new StringBuffer());

            //create thread for reading errorStream (process' stderr)
            StreamReaderThread errThread = new StreamReaderThread(proc.getErrorStream(), err);

            //start both threads
            outThread.start();
            errThread.start();

            //wait for process to end
            int result = proc.waitFor();

            //finish reading whatever's left in the buffers
            outThread.join();
            errThread.join();

            if (result != 0) {
//                entryStore.stopLocalServer(entryStore.getRestarting());
                entryStore.stopLocalServer();
                String errString = err.toString();

                // If the server couldn't start for a known reason, try again on another port
                //  Retry up to 10 times
                if ((result==35584 || errString.indexOf("Error binding to port") >= 0) &&
                        Integer.parseInt(EntryStore.getLocalPort()) < Integer.parseInt(Constants.LOCAL_ADDE_PORT) + 10) {
                    EntryStore.setLocalPort(EntryStore.nextLocalPort());
//                    entryStore.startLocalServer(entryStore.getRestarting());
                    entryStore.startLocalServer();
                }
            }
        } catch (InterruptedException e) {
            McservEvent type = McservEvent.DIED;
//            if (entryStore.getRestarting()) {
                type = McservEvent.STARTED;
//            }
            EventBus.publish(type);
        } catch (Exception e) {
            EventBus.publish(McservEvent.DIED);
        }
    }

    /**
     * 
     */
    public void stopProcess() {
        proc.destroy();
    }

//    /**
//     * 
//     */
//    public String toString() {
//        return String.format("[AddeThread@%x: ADDE_ENV=%s, ADDE_COMMANDS=%s]", hashCode(), ADDE_ENV, ADDE_COMMANDS);
//    }

    /**
     * Thread to read the stderr and stdout of mcservl
     */
    private static class StreamReaderThread extends Thread {
        /** */
        private final StringBuffer mOut;

        /** */
        private final InputStreamReader mIn;

        /** */
        public StreamReaderThread(final InputStream in, final StringBuffer out) {
            mOut = out;
            mIn = new InputStreamReader(in);
        }

        /** */
        public void run() {
            int ch;
            try {
                while (-1 != (ch = mIn.read())) {
                    mOut.append((char)ch);
                }
            } catch (Exception e) {
                mOut.append("\nRead error: "+e.getMessage());
            }
        }
    }
}