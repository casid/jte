package org.apache.jasper.compiler;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.servlet.JspCServletContext;
import org.apache.jasper.servlet.TldScanner;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import java.io.*;
import java.net.URL;
import java.util.Map;

public class JtpParser {
    public static Node.Nodes parse(String relativeFilePath, byte[] input, URL resourceBase, boolean tag) throws JasperException, IOException {
        JspC jspc = new JspC();

        PrintWriter log = new PrintWriter(System.out);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ServletContext context = new JspCServletContext(log, resourceBase, classLoader, jspc.isValidateXml(), jspc.isBlockExternal());

        var scanner = new TldScanner(context, true, jspc.isValidateTld(), jspc.isBlockExternal());
        scanner.setClassLoader(classLoader);

        try {
            scanner.scan();
        } catch (SAXException e) {
            throw new JasperException(e);
        }

        var tldCache = new TldCache(context, scanner.getUriTldResourcePathMap(), scanner.getTldResourcePathTaglibXmlMap());
        context.setAttribute(TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME, tldCache);

        var jspConfig = new JspConfig(context);

        JspRuntimeContext runtimeContext = new JspRuntimeContext(context, jspc);
        JspCompilationContext compilationContext = new JspCompilationContext("", new Options() {
            @Override
            public boolean getErrorOnUseBeanInvalidClassAttribute() {
                return true;
            }

            @Override
            public boolean getKeepGenerated() {
                return false;
            }

            @Override
            public boolean isPoolingEnabled() {
                return false;
            }

            @Override
            public boolean getMappedFile() {
                return false;
            }

            @Override
            public boolean getClassDebugInfo() {
                return false;
            }

            @Override
            public int getCheckInterval() {
                return 0;
            }

            @Override
            public boolean getDevelopment() {
                return false;
            }

            @Override
            public boolean getDisplaySourceFragment() {
                return false;
            }

            @Override
            public boolean isSmapSuppressed() {
                return false;
            }

            @Override
            public boolean isSmapDumped() {
                return false;
            }

            @Override
            public boolean getTrimSpaces() {
                return false;
            }

            @Override
            public String getIeClassId() {
                return null;
            }

            @Override
            public File getScratchDir() {
                return null;
            }

            @Override
            public String getClassPath() {
                return null;
            }

            @Override
            public String getCompiler() {
                return null;
            }

            @Override
            public String getCompilerTargetVM() {
                return null;
            }

            @Override
            public String getCompilerSourceVM() {
                return null;
            }

            @Override
            public String getCompilerClassName() {
                return null;
            }

            @Override
            public TldCache getTldCache() {
                return tldCache;
            }

            @Override
            public String getJavaEncoding() {
                return "UTF-8";
            }

            @Override
            public boolean getFork() {
                return false;
            }

            @Override
            public JspConfig getJspConfig() {
                return jspConfig;
            }

            @Override
            public boolean isXpoweredBy() {
                return false;
            }

            @Override
            public TagPluginManager getTagPluginManager() {
                return null;
            }

            @Override
            public boolean genStringAsCharArray() {
                return false;
            }

            @Override
            public int getModificationTestInterval() {
                return 0;
            }

            @Override
            public boolean getRecompileOnFail() {
                return false;
            }

            @Override
            public boolean isCaching() {
                return false;
            }

            @Override
            public Map<String, TagLibraryInfo> getCache() {
                return null;
            }

            @Override
            public int getMaxLoadedJsps() {
                return 0;
            }

            @Override
            public int getJspIdleTimeout() {
                return 0;
            }

            @Override
            public boolean getStrictQuoteEscaping() {
                return false;
            }

            @Override
            public boolean getQuoteAttributeEL() {
                return false;
            }
        }, context, null, runtimeContext);

        Compiler compiler = compilationContext.createCompiler();
        compiler.pageInfo = new PageInfo(null, "", tag);

        ParserController parserController = new ParserController(compilationContext, compiler);

        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(input));
        ErrorDispatcher errorDispatcher = new ErrorDispatcher(true);

        JspReader jspReader = new JspReader(compilationContext, relativeFilePath, reader, errorDispatcher);

        return Parser.parse(parserController, jspReader, null, tag, false, null, "UTF-8", "UTF-8", true, false);
    }
}