package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author Emilio Zottel (4AHIF)
 * @since 10.11.2022, Do.
 */
public class ClientSocket {
    private final Socket socket;
    private final InputStream socketInputStream;
    private final OutputStream socketOutputStream;
    private final byte[] buffer;
    private boolean active;

    public ClientSocket(Socket socket, int bufferSize) throws IOException {
        this.socket = socket;
        this.socketInputStream = socket.getInputStream();
        this.socketOutputStream = socket.getOutputStream();
        this.buffer = new byte[bufferSize];
        this.active = true;
    }


    public int receiveBuffer(boolean logDisconnect) {
        try {
            return socketInputStream.read(buffer);
        } catch (IOException e) {
            active = false;
            if (logDisconnect) printDisconnectMessage();
            return Integer.MIN_VALUE;
        }
    }

    public boolean sendBuffer(byte[] buffer, boolean logDisconnect) {
        try {
            socketOutputStream.write(buffer);
            return true;
        } catch (IOException e) {
            active = false;
            if (logDisconnect) printDisconnectMessage();
            return false;
        }
    }

    public void printDisconnectMessage() {
        printDisconnectMessage(System.err);
    }

    private void printDisconnectMessage(PrintStream ps) {
        ps.println("-Client disconnected: " + this);
    }

    public void connect(SocketAddress endpoint) throws IOException {
        socket.connect(endpoint);
    }

    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        socket.connect(endpoint, timeout);
    }

    public void bind(SocketAddress bindpoint) throws IOException {
        socket.bind(bindpoint);
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public SocketAddress getLocalSocketAddress() {
        return socket.getLocalSocketAddress();
    }

    public SocketChannel getChannel() {
        return socket.getChannel();
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public boolean getTcpNoDelay() throws SocketException {
        return socket.getTcpNoDelay();
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        socket.setTcpNoDelay(on);
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        socket.setSoLinger(on, linger);
    }

    public int getSoLinger() throws SocketException {
        return socket.getSoLinger();
    }

    public void sendUrgentData(int data) throws IOException {
        socket.sendUrgentData(data);
    }

    public boolean getOOBInline() throws SocketException {
        return socket.getOOBInline();
    }

    public void setOOBInline(boolean on) throws SocketException {
        socket.setOOBInline(on);
    }

    public synchronized int getSoTimeout() throws SocketException {
        return socket.getSoTimeout();
    }

    public synchronized void setSoTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public synchronized int getSendBufferSize() throws SocketException {
        return socket.getSendBufferSize();
    }

    public synchronized void setSendBufferSize(int size) throws SocketException {
        socket.setSendBufferSize(size);
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        return socket.getReceiveBufferSize();
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        socket.setReceiveBufferSize(size);
    }

    public boolean getKeepAlive() throws SocketException {
        return socket.getKeepAlive();
    }

    public void setKeepAlive(boolean on) throws SocketException {
        socket.setKeepAlive(on);
    }

    public int getTrafficClass() throws SocketException {
        return socket.getTrafficClass();
    }

    public void setTrafficClass(int tc) throws SocketException {
        socket.setTrafficClass(tc);
    }

    public boolean getReuseAddress() throws SocketException {
        return socket.getReuseAddress();
    }

    public void setReuseAddress(boolean on) throws SocketException {
        socket.setReuseAddress(on);
    }

    public synchronized void close() throws IOException {
        socket.close();
    }

    public void shutdownInput() throws IOException {
        socket.shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        socket.shutdownOutput();
    }


    public byte[] getBuffer() {
        return buffer;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public boolean isBound() {
        return socket.isBound();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean isInputShutdown() {
        return socket.isInputShutdown();
    }

    public boolean isOutputShutdown() {
        return socket.isOutputShutdown();
    }

    public boolean isActive() {
        return active;

    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        socket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    public <T> Socket setOption(SocketOption<T> name, T value) throws IOException {
        return socket.setOption(name, value);
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        return socket.getOption(name);
    }

    public Set<SocketOption<?>> supportedOptions() {
        return socket.supportedOptions();
    }


    @Override
    public String toString() {
        return socket.toString();
    }

}
