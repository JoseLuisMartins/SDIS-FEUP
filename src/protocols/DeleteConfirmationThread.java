package protocols;


import file.FileInfo;
import logic.Message;
import logic.MessageType;
import logic.Utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DeleteConfirmationThread implements Runnable {


    public DeleteConfirmationThread() {

    }

    @Override
    public void run() {
        Utils.confirmationDeleteThreadRunning=true;
        try {
            ServerSocket welcomeSocket = new ServerSocket(Utils.mc.getPort());

            while (true) {
                Socket connectionSocket = welcomeSocket.accept();

                InputStream in = connectionSocket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                int len = dis.readInt();
                byte[] confirmationBytes = new byte[len];
                if (len > 0) {
                    dis.readFully(confirmationBytes);
                }

                Message confirmation = new Message(confirmationBytes);

                if(confirmation.getType() == MessageType.DELETED_CONFIRMATION) {
                    System.out.println("Received delete confirmation from file with fileId(" + confirmation.getFileId() +")\n");

                    FileInfo fileInfo = Utils.metadata.getBackupFilesMetadata().get(confirmation.getFileId());

                    if (fileInfo != null && fileInfo.isOnDeleteProcess()) {
                        fileInfo.deletePeerChunks(confirmation.getSenderId());

                        if (fileInfo.isFileFullyDeleted()) {
                            Utils.metadata.removeFile(fileInfo.getFileId());
                            break;
                        }
                    }
                }
            }

            welcomeSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.confirmationDeleteThreadRunning=false;
    }
}
