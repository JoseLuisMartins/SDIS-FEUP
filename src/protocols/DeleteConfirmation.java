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
    private FileInfo fileInfo;

    public DeleteConfirmation(FileInfo fileInfo) {
        this.fileInfo=fileInfo;
    }

    @Override
    public void run() {
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

                this.fileInfo.deletePeerChunks(confirmation.getSenderId());

                if(this.fileInfo.isFileFullyDeleted()) {//TODO probably a problem because multiple peers can modify metadata at the same time
                    Utils.metadata.removeFile(fileInfo.getFileId());
                    break;
                }
            }

            welcomeSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
