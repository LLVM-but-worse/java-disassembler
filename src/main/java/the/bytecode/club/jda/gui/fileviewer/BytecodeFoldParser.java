package the.bytecode.club.jda.gui.fileviewer;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.Fold;

import java.util.List;

public class BytecodeFoldParser extends CurlyFoldParser {
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Fold> getFolds(RSyntaxTextArea textArea) {
    	return super.getFolds(textArea);
    }
}
