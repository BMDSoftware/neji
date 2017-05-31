/*
 * Copyright (c) 2016 BMD Software and University of Aveiro.
 *
 * Neji is a flexible and powerful platform for biomedical information extraction from text.
 *
 * This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * This project is a free software, you are free to copy, distribute, change and transmit it.
 * However, you may not use it for commercial purposes.
 *
 * It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package pt.ua.tm.neji.web.cli;

import cc.mallet.util.MalletLogger;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextConfiguration;
import pt.ua.tm.neji.context.InputFormat;
import pt.ua.tm.neji.context.OutputFormat;
import pt.ua.tm.neji.core.Constants;
import pt.ua.tm.neji.core.parser.ParserLanguage;
import pt.ua.tm.neji.core.parser.ParserLevel;
import pt.ua.tm.neji.core.parser.ParserTool;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.logger.LoggingOutputStream;
import pt.ua.tm.neji.web.server.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import static pt.ua.tm.neji.web.WebConstants.DICTIONARIES_PATH;
import static pt.ua.tm.neji.web.WebConstants.MODELS_PATH;

/**
 * Main application for web services features.
 *
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class WebMain {

    private static final String HEADER = "\nNeji: modular biomedical concept recognition made easy, fast and accessible.";
    private static final String USAGE = "[-t <threads>] [-v]";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/neji.";

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(WebMain.class);

    public static void main(String[] args) {

        // Set JSP to use Standard JavaC always
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        options.addOption("h", "help", false, "Print this usage information.");
        options.addOption("v", "verbose", false, "Verbose mode.");
        options.addOption("d", "dictionaires", true, "Folder that contains the dictionaries.");
        options.addOption("m", "models", true, "Folder that contains the ML models.");

        options.addOption("port", "port", true, "Server port.");
        options.addOption("c", "configuration", true, "Configuration properties file.");

        options.addOption("t", "threads", true, "Number of threads. By default, if more than one core is available, it is the number of cores minus 1.");

        CommandLine commandLine;
        try {
            // Parse the program arguments
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments.", ex);
            return;
        }

        // Show help text
        if (commandLine.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(150, "./nejiWeb.sh " + USAGE, HEADER, options, FOOTER);
            return;
        }

        // Get threads
        int numThreads = Runtime.getRuntime().availableProcessors() - 1;
        numThreads = numThreads > 0 ? numThreads : 1;
        if (commandLine.hasOption('t')) {
            String threadsText = commandLine.getOptionValue('t');
            numThreads = Integer.parseInt(threadsText);
            if (numThreads <= 0 || numThreads > 32) {
                logger.error("Illegal number of threads. Must be between 1 and 32.");
                return;
            }
        }

        // Get port
        int port = 8010;
        if (commandLine.hasOption("port")) {
            String portString = commandLine.getOptionValue("port");
            port = Integer.parseInt(portString);
        }

        // Get configuration
        String configurationFile = null;
        Properties configurationProperties = null;
        if (commandLine.hasOption("configuration")) {
            configurationFile = commandLine.getOptionValue("configuration");
            try {
                configurationProperties = new Properties();
                configurationProperties.load(new FileInputStream(configurationFile));
            } catch (IOException e) {
                configurationProperties = null;
            }
        }
        if (configurationProperties != null && !configurationProperties.isEmpty()) {
            ServerConfiguration.initialize(configurationProperties);
        } else {
            ServerConfiguration.initialize();
        }

        // Set system proxy
        if (!ServerConfiguration.getInstance().getProxyURL().isEmpty() && !ServerConfiguration.getInstance().getProxyPort().isEmpty()) {
            System.setProperty("https.proxyHost", ServerConfiguration.getInstance().getProxyURL());
            System.setProperty("https.proxyPort", ServerConfiguration.getInstance().getProxyPort());
            System.setProperty("http.proxyHost", ServerConfiguration.getInstance().getProxyURL());
            System.setProperty("http.proxyPort", ServerConfiguration.getInstance().getProxyPort());

            if (!ServerConfiguration.getInstance().getProxyUsername().isEmpty()) {
                final String proxyUser = ServerConfiguration.getInstance().getProxyUsername();
                final String proxyPassword = ServerConfiguration.getInstance().getProxyPassword();
                System.setProperty("https.proxyUser", proxyUser);
                System.setProperty("https.proxyPassword", proxyPassword);
                System.setProperty("http.proxyUser", proxyUser);
                System.setProperty("http.proxyPassword", proxyPassword);
                Authenticator.setDefault(
                        new Authenticator() {
                            @Override
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                            }
                        }
                );
            }
        }

        // Get verbose mode
        boolean verbose = commandLine.hasOption('v');
        Constants.verbose = verbose;

        if (Constants.verbose) {
            MalletLogger.getGlobal().setLevel(Level.INFO);
            // Redirect sout
            LoggingOutputStream los = new LoggingOutputStream(LoggerFactory.getLogger("stdout"), false);
            System.setOut(new PrintStream(los, true));

            // Redirect serr
            los = new LoggingOutputStream(LoggerFactory.getLogger("sterr"), true);
            System.setErr(new PrintStream(los, true));
        } else {
            MalletLogger.getGlobal().setLevel(Level.OFF);
        }

        // Get dictionaries folder
        String dictionariesFolder = null;
        if (commandLine.hasOption('d')) {
            dictionariesFolder = commandLine.getOptionValue('d');

            File test = new File(dictionariesFolder);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified dictionaries path is not a folder or is not readable.");
                return;
            }
            dictionariesFolder = test.getAbsolutePath();
            dictionariesFolder += File.separator;
        }

        // Get models folder
        String modelsFolder = null;
        if (commandLine.hasOption('m')) {
            modelsFolder = commandLine.getOptionValue('m');

            File test = new File(modelsFolder);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified models path is not a folder or is not readable.");
                return;
            }
            modelsFolder = test.getAbsolutePath();
            modelsFolder += File.separator;
        }

        // Redirect JUL to SLF4
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Output formats (All)
        List<OutputFormat> outputFormats = new ArrayList<>();
        outputFormats.add(OutputFormat.A1);
        outputFormats.add(OutputFormat.B64);
        outputFormats.add(OutputFormat.BC2);
        outputFormats.add(OutputFormat.BIOC);
        outputFormats.add(OutputFormat.CONLL);
        outputFormats.add(OutputFormat.JSON);
        outputFormats.add(OutputFormat.NEJI);
        outputFormats.add(OutputFormat.PIPE);
        outputFormats.add(OutputFormat.PIPEXT);
        outputFormats.add(OutputFormat.XML);
        outputFormats.add(OutputFormat.A1_MIN);

        // Context is built through a descriptor first, so that the pipeline can be validated before any processing
        ContextConfiguration descriptor = null;
        try {
            descriptor = new ContextConfiguration.Builder()
                    .withInputFormat(InputFormat.RAW) // HARDCODED
                    .withOutputFormats(outputFormats)
                    .withParserTool(ParserTool.GDEP)
                    .withParserLanguage(ParserLanguage.ENGLISH)
                    .withParserLevel(ParserLevel.CHUNKING)
                    .build();

        } catch (NejiException ex) {
            ex.printStackTrace();
            System.exit(1);
        }


        // Create resources dirs if they don't exist
        try {
            File dictionariesDir = new File(DICTIONARIES_PATH);
            File modelsDir = new File(MODELS_PATH);
            if (!dictionariesDir.exists()) {
                dictionariesDir.mkdirs();
                (new File(dictionariesDir, "_priority")).createNewFile();
            }
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
                (new File(modelsDir, "_priority")).createNewFile();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // Contenxt
        Context context = new Context(descriptor, MODELS_PATH, DICTIONARIES_PATH);

        // Start server
        try {
            Server server = Server.getInstance();
            server.initialize(context, port, numThreads);

            server.start();
            logger.info("Server started at localhost:{}", port);
            logger.info("Press Cmd-C / Ctrl+C to shutdown the server...");

            server.join();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("Shutting down the server...");
        }
    }

}
