package org.delcom.ifs25007springbootcashflow.framework.view;

/** Menampung file hasil unggahan sampai tombol Simpan ditekan. */
public class PendingUpload {
    private String name;
    private byte[] data;

    public synchronized void set(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public synchronized boolean isPresent() {
        return data != null;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized byte[] getData() {
        return data;
    }
}
