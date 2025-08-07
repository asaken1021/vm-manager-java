package net.asaken1021.vmmanager;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import net.asaken1021.vmmanager.util.ConnectException;
import net.asaken1021.vmmanager.util.DomainLookupException;
import net.asaken1021.vmmanager.util.VMManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ServerEndpoint("/vnc/{vmUuid}")
public class VncProxyEndpoint {

    private ExecutorService executor;
    private Socket vncSocket;
    private PipedOutputStream webSocketMessageOutput;

    private VMManager vmm;

    public VncProxyEndpoint(String uri) {
        try {
			this.vmm = new VMManager(uri);
		} catch (ConnectException e) {
			e.printStackTrace();
		}
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("vmUuid") String vmUuid) throws IOException {
        this.executor = Executors.newFixedThreadPool(2);
        this.webSocketMessageOutput = new PipedOutputStream();
        PipedInputStream webSocketMessageInput = new PipedInputStream(this.webSocketMessageOutput);

        Optional<Integer> portOpt;
		try {
			portOpt = Optional.of(Integer.valueOf(this.vmm.getVm(UUID.fromString(vmUuid)).getVmGraphics().getPort()));
		} catch (DomainLookupException e) {
			portOpt = Optional.empty();
			e.printStackTrace();
		}
        if (portOpt.isEmpty()) {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "VM not found."));
            return;
        }

        try {
            String vncAddr = this.vmm.getVm(UUID.fromString(vmUuid)).getVmGraphics().getAddress();

            this.vncSocket = new Socket(vncAddr, portOpt.get());

            // WebSocketメッセージハンドラを設定
            // 受信したメッセージはすべてPipedOutputStreamに書き込む
            session.addMessageHandler(ByteBuffer.class, message -> {
                try {
                    byte[] buffer = new byte[message.remaining()];
                    message.get(buffer);
                    webSocketMessageOutput.write(buffer);
                    webSocketMessageOutput.flush();
                } catch (IOException e) {
                    // パイプが閉じられた場合など
                    System.err.println("Error writing to PipedOutputStream: " + e.getMessage());
                    closeAllResources();
                }
            });

            // 2つの転送スレッドを開始する
            executor.submit(() -> forwardVncToWebSocket(session, this.vncSocket));
            executor.submit(() -> forwardWebSocketToVnc(webSocketMessageInput, this.vncSocket));

        } catch (IOException | DomainLookupException e) {
            closeAllResources();
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Backend failed to connect to VNC."));
        }
    }

    /**
     * VNCサーバーからのデータを読み取り、WebSocketクライアントへ送信する。
     */
    private void forwardVncToWebSocket(Session session, Socket socket) {
        try (InputStream vncInput = socket.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = vncInput.read(buffer)) != -1) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendBinary(ByteBuffer.wrap(buffer, 0, bytesRead));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            // ソケットの読み取りエラー
        } finally {
            closeAllResources();
        }
    }

    /**
     * WebSocketからのデータを(PipedInputStream経由で)読み取り、VNCサーバーへ送信する。
     */
    private void forwardWebSocketToVnc(InputStream webSocketInput, Socket socket) {
        try (OutputStream vncOutput = socket.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = webSocketInput.read(buffer)) != -1) {
                vncOutput.write(buffer, 0, bytesRead);
                vncOutput.flush();
            }
        } catch (IOException e) {
            // ソケットの書き込みエラー
        } finally {
            closeAllResources();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        closeAllResources();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
        closeAllResources();
    }

    private void closeAllResources() {
        try {
            if (this.webSocketMessageOutput != null) {
                this.webSocketMessageOutput.close();
            }
        } catch (IOException e) {
            // ignore
        }
        try {
            if (this.vncSocket != null && !this.vncSocket.isClosed()) {
                this.vncSocket.close();
            }
        } catch (IOException e) {
            // ignore
        }
        if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdownNow();
        }
    }
}