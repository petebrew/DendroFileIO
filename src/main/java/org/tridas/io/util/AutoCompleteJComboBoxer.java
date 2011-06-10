package org.tridas.io.util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public class AutoCompleteJComboBoxer extends PlainDocument {

	private static final long serialVersionUID = -3266110852693588487L;
	private final JComboBox comboBox;
    private ComboBoxModel model;
    private JTextComponent editor;
    private boolean hidePopupOnFocusLoss;
 
    public AutoCompleteJComboBoxer(JComboBox comboBox) {
        this.comboBox = comboBox;
        comboBox.setEditable(true);
        model = comboBox.getModel();
        editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        editor.setDocument(this);
        // Bug 5100422 on Java 1.5: Editable JComboBox won't hide popup when tabbing out
        hidePopupOnFocusLoss = System.getProperty("java.version").startsWith("1.5");
               
        // Highlight whole text when focus gets lost
        editor.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                highlightCompletedText(0);
                // Workaround for Bug 5100422 - Hide Popup on focus loss
                if (hidePopupOnFocusLoss) AutoCompleteJComboBoxer.this.comboBox.setPopupVisible(false);
            }
            
        });
        // Highlight whole text when user hits enter
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    highlightCompletedText(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    AutoCompleteJComboBoxer.this.comboBox.setSelectedIndex(0);
                    AutoCompleteJComboBoxer.this.editor.setText(AutoCompleteJComboBoxer.this.comboBox.getSelectedItem().toString());
                    highlightCompletedText(0);
                }
            }
        });
 
        // Handle initially selected object
        Object selected = comboBox.getSelectedItem();
        if (selected != null) editor.setText(selected.toString());
    }
 
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // construct the resulting string
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(offs, currentText.length());
        String futureText = beforeOffset + str + afterOffset;
 
        // lookup and select a matching item
        Object item = lookupItem(futureText);
        if (item != null) {
            comboBox.setSelectedItem(item);
        } else {
            // keep old item selected if there is no match
            item = comboBox.getSelectedItem();
            // imitate no insert (later on offs will be incremented by str.length(): selection won't move forward)
            offs = offs-str.length();
            // provide feedback to the user that his input has been received but can not be accepted
            comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
        }
 
        // remove all text and insert the completed string
        super.remove(0, getLength());
        super.insertString(0, item.toString(), a);
 
        // if the user selects an item via mouse the the whole string will be inserted.
        // highlight the entire text if this happens.
        if (item.toString().equals(str) && offs==0) {
            highlightCompletedText(0);
        } else {
            highlightCompletedText(offs+str.length());
            // show popup when the user types
            comboBox.setPopupVisible(true);
        }
    }
 
    private void highlightCompletedText(int start) {
        editor.setCaretPosition(getLength());
        editor.moveCaretPosition(start);
    }
 
    private Object lookupItem(String pattern) {
        Object selectedItem = model.getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        } else {
            // iterate over all items
            for (int i=0, n=model.getSize(); i < n; i++) {
                Object currentItem = model.getElementAt(i);
                // current item starts with the pattern?
                if (startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }
 
    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }
 
}