package de.invesdwin.util.swing.listener;

import javax.annotation.concurrent.Immutable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@Immutable
public class DocumentListenerSupport implements DocumentListener {

    @Override
    public void insertUpdate(final DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        update(e);
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        update(e);
    }

    protected void update(final DocumentEvent e) {}

}
