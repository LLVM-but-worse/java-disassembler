package the.bytecode.club.jda.gui.fileviewer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class JDAURLHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        throw new UnsupportedOperationException();
    }
}
