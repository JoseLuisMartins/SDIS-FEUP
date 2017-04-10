package protocols;


import file.FileInfo;
import logic.Message;
import logic.Utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DeleteConfirmation implements Runnable {


    public DeleteConfirmation() {

    }

    @Override
    public void run() {
        Utils.confirmationDeleteThreadRunning=true;
        try {
            ServerSocket welcomeSocket = new ServerSocket(Utils.mc.getPort());

            while (true) {//TODO: instead of while true run just for like 5 seconds ...
                Socket connectionSocket = welcomeSocket.accept();

                InputStream in = connectionSocket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                int len = dis.readInt();
                byte[] confirmationBytes = new byte[len];
                if (len > 0) {
                    dis.readFully(confirmationBytes);
                }

                Message confirmation = new Message(confirmationBytes);
                FileInfo fileInfo = Utils.metadata.getBackupFilesMetadata().get(confirmation.getFileId());

                if(fileInfo != null && fileInfo.isOnDeleteProcess()) {
                    fileInfo.deletePeerChunks(confirmation.getSenderId());

                    if (fileInfo.isFileFullyDeleted()) {
                        Utils.metadata.removeFile(fileInfo.getFileId());
                        break;
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
