package com.mrikso.texteditor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.Toast;

import com.mrikso.codeeditor.lang.Language;
import com.mrikso.codeeditor.lang.LanguageJava;
import com.mrikso.codeeditor.util.Document;
import com.mrikso.codeeditor.util.DocumentProvider;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.view.ColorScheme;
import com.mrikso.codeeditor.view.FreeScrollingTextField;
import com.mrikso.codeeditor.view.YoyoNavigationMethod;
import com.mrikso.codeeditor.view.autocomplete.AutoCompletePanel;

import java.io.File;


public class TextEditor extends FreeScrollingTextField {
    private Document _inputtingDoc;
    private boolean _isWordWrap;
    private Context mContext;
    private String _lastSelectFile;
    private int _index;
    private Toast toast;
    /*
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case ReadThread.MSG_READ_OK:
                    setText(msg.obj.toString());
                    break;
                case ReadThread.MSG_READ_FAIL:
                    showToast("打开失败");
                    break;
                case WriteThread.MSG_WRITE_OK:
                    showToast("保存成功");
                    break;
                case WriteThread.MSG_WRITE_FAIL:
                    showToast("保存失败");
                    break;
            }
        }
    };
/*/
    public TextEditor(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public TextEditor(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        init();
    }

    private void init() {
        setVerticalScrollBarEnabled(true);
        setTypeface(Typeface.MONOSPACE);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        //设置字体大小
        float size = TypedValue.applyDimension(2, BASE_TEXT_SIZE_PIXELS, dm);
        setTextSize((int) size);
        setShowLineNumbers(true);
        //setAutoCompete(true);
        setHighlightCurrentRow(true);
        setWordWrap(true);
        setAutoComplete(true);
        setAutoIndent(true);
        setUseGboard(true);
        setAutoIndentWidth(2);
        setLanguage(LanguageJava.getInstance());
        setNavigationMethod(new YoyoNavigationMethod(this));
        int textColor = Color.BLACK;// 默认文字颜色
        int selectionText = Color.argb(255, 0, 120, 215);//选择文字颜色
        setTextColor(textColor);
        setTextHighlightColor(selectionText);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO: Implement this method
        super.onLayout(changed, left, top, right, bottom);
        if (_index != 0 && right > 0) {
            moveCaret(_index);
            _index = 0;
        }
    }

    public void setKeywordColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.KEYWORD, color);
    }

    public void setBaseWordColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.NAME, color);
    }

    public void setStringColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.STRING, color);
    }

    public void setCommentColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.COMMENT, color);
    }

    public void setBackgroundColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.BACKGROUND, color);
    }

    public void setTextColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.FOREGROUND, color);
    }

    public void setTextHighlightColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.SELECTION_BACKGROUND, color);
    }

    public void setLanguage(Language language){
        AutoCompletePanel.setLanguage(language);
        Lexer.setLanguage(language);
    }

    public String getSelectedText() {
        // TODO: Implement this method
        return hDoc.subSequence(getSelectionStart(), getSelectionEnd() - getSelectionStart()).toString();
    }

    public void gotoLine(int line) {
        if (line > hDoc.getRowCount()) {
            line = hDoc.getRowCount();

        }
        int i = getText().getLineOffset(line - 1);
        setSelection(i);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        final int filteredMetaState = event.getMetaState() & ~KeyEvent.META_CTRL_MASK;
        if (KeyEvent.metaStateHasNoModifiers(filteredMetaState)) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_A:
                    selectAll();
                    return true;
                case KeyEvent.KEYCODE_X:
                    cut();
                    return true;
                case KeyEvent.KEYCODE_C:
                    copy();
                    return true;
                case KeyEvent.KEYCODE_V:
                    paste();
                    return true;
            }
        }
        return super.onKeyShortcut(keyCode, event);
    }

    @Override
    public void setWordWrap(boolean enable) {
        // TODO: Implement this method
        _isWordWrap = enable;
        super.setWordWrap(enable);
    }

    public DocumentProvider getText() {
        return createDocumentProvider();
    }

    public void setText(CharSequence c) {
        Document doc = new Document(this);
        doc.setWordWrap(_isWordWrap);
        doc.setText(c);
        setDocumentProvider(new DocumentProvider(doc));
    }

    public File getOpenedFile() {
        if (_lastSelectFile != null)
            return new File(_lastSelectFile);

        return null;
    }

    public void setOpenedFile(String file) {
        _lastSelectFile = file;
    }

    public void insert(int idx, String text) {
        selectText(false);
        moveCaret(idx);
        paste(text);
    }

    public void replaceAll(CharSequence c) {
        replaceText(0, getLength() - 1, c.toString());
    }

    public void setSelection(int index) {
        selectText(false);
        if (!hasLayout())
            moveCaret(index);
        else
            _index = index;
    }

    public void undo() {

        DocumentProvider doc = createDocumentProvider();
        int newPosition = doc.undo();

        if (newPosition >= 0) {
            //TODO editor.setEdited(false);
            // if reached original condition of file
            setEdited(true);
            respan();
            selectText(false);
            moveCaret(newPosition);
            invalidate();
        }

    }

    public void redo() {

        DocumentProvider doc = createDocumentProvider();
        int newPosition = doc.redo();

        if (newPosition >= 0) {
            setEdited(true);

            respan();
            selectText(false);
            moveCaret(newPosition);
            invalidate();
        }

    }
/*
    public void open(String filename) {
        _lastSelectFile = filename;

        File inputFile = new File(filename);
        _inputtingDoc = new Document(this);
        _inputtingDoc.setWordWrap(this.isWordWrap());
        ReadThread readThread = new ReadThread(inputFile.getAbsolutePath(), handler);
        readThread.start();
    }

    /**
     * 保存文件
     * * @param file
     */
/*
    public void save(String file) {
        WriteThread writeThread = new WriteThread(getText().toString(), file, handler);
        writeThread.start();
    }


 */
    private void showToast(CharSequence text) {
        if (toast == null) {
            toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }
}

