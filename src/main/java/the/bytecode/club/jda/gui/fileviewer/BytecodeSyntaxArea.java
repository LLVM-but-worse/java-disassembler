package the.bytecode.club.jda.gui.fileviewer;

import org.fife.ui.rsyntaxtextarea.*;

import javax.swing.event.HyperlinkEvent;
import java.net.MalformedURLException;
import java.net.URL;

public class BytecodeSyntaxArea extends RSyntaxTextArea {
    public BytecodeSyntaxArea() {
        setSyntaxEditingStyle(BytecodeTokenizer.SYNTAX_STYLE_BYTECODE);

        setLinkScanningMask(0);
        setLinkGenerator(new BytecodeLinkGenerator());
        addHyperlinkListener(e -> {
            URL url = e.getURL();
            String data = url.getFile();
            switch (url.getProtocol()) {
                case "label":
                    System.out.println(data);
                    setCaretPosition(0);
                    break;
            }
        });
    }

    private static class BytecodeLinkGenerator implements LinkGenerator {
        @Override
        public LinkGeneratorResult isLinkAtOffset(RSyntaxTextArea textArea, int offs) {
            Token t = textArea.modelToToken(offs);
            if (t.getType() == TokenTypes.PREPROCESSOR) {
                return new LinkGeneratorResult() {
                    @Override
                    public HyperlinkEvent execute() {
                        try {
                            URL url = new URL(null, "label:" + t.getLexeme(), new JDAURLHandler());
                            return new HyperlinkEvent(textArea, HyperlinkEvent.EventType.ACTIVATED, url);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public int getSourceOffset() {
                        return offs;
                    }
                };
            }
            return null;
        }
    }
}
