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

import java.util.Properties;

/**
 * Server configuration.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @author André Santos (<a href="mailto:andre.jeronimo@ua.pt">andre.jeronimo@ua.pt</a>)
 * @version 2.0
 * @since 1.0
 */
public class ServerConfiguration {
    private String proxyURL;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    private String httpsKeystoreFile;
    private String httpsKeystorePassword;
    private String httpsTruststoreFile;
    private String httpsTruststorePassword;

    private static ServerConfiguration instance = null;

    public ServerConfiguration(final Properties properties) {
        proxyURL = properties.getProperty("proxy.url");
        proxyPort = properties.getProperty("proxy.port");
        proxyUsername = properties.getProperty("proxy.username");
        proxyPassword = properties.getProperty("proxy.password");
        httpsKeystoreFile = properties.getProperty("https.keystore.file");
        httpsKeystorePassword = properties.getProperty("https.keystore.password");
        httpsTruststoreFile = properties.getProperty("https.truststore.file");
        httpsTruststorePassword = properties.getProperty("https.truststore.password");
    }

    public ServerConfiguration() {
        proxyURL = "";
        proxyPort = "";
        proxyUsername = "";
        proxyPassword = "";
        httpsKeystoreFile = "conf/keystore.jks";
        httpsKeystorePassword = "BMD4key";
        httpsTruststoreFile = "conf/truststore.jks";
        httpsTruststorePassword = "BMD4key";
    }

    public static void initialize(final Properties properties) {
        instance = new ServerConfiguration(properties);
    }

    public static void initialize() {
        instance = new ServerConfiguration();
    }

    public static ServerConfiguration getInstance() {
        if (instance == null) {
            throw new RuntimeException("Please call initialize method before getting server configuration instance.");
        }
        return instance;
    }

    public String getProxyURL() {
        return proxyURL;
    }

    public void setProxyURL(final String proxyURL) {
        this.proxyURL = proxyURL;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getHttpsKeystoreFile() {
        return httpsKeystoreFile;
    }

    public void setHttpsKeystoreFile(final String httpsKeystoreFile) {
        this.httpsKeystoreFile = httpsKeystoreFile;
    }

    public String getHttpsTruststoreFile() {
        return httpsTruststoreFile;
    }

    public void setHttpsTruststoreFile(final String httpsTruststoreFile) {
        this.httpsTruststoreFile = httpsTruststoreFile;
    }

    public String getHttpsKeystorePassword() {
        return httpsKeystorePassword;
    }

    public String getHttpsTruststorePassword() {
        return httpsTruststorePassword;
    }

    public void setHttpsTruststorePassword(final String httpsTruststorePassword) {
        this.httpsTruststorePassword = httpsTruststorePassword;
    }

    public void setHttpsKeystorePassword(final String httpsKeystorePassword) {
        this.httpsKeystorePassword = httpsKeystorePassword;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerConfiguration that = (ServerConfiguration) o;

        if (proxyURL != null ? !proxyURL.equals(that.proxyURL) : that.proxyURL != null) return false;
        if (proxyPort != null ? !proxyPort.equals(that.proxyPort) : that.proxyPort != null) return false;
        if (proxyUsername != null ? !proxyUsername.equals(that.proxyUsername) : that.proxyUsername != null)
            return false;
        if (proxyPassword != null ? !proxyPassword.equals(that.proxyPassword) : that.proxyPassword != null)
            return false;
        if (httpsKeystoreFile != null ? !httpsKeystoreFile.equals(that.httpsKeystoreFile) : that.httpsKeystoreFile != null)
            return false;
        if (httpsKeystorePassword != null ? !httpsKeystorePassword.equals(that.httpsKeystorePassword) : that.httpsKeystorePassword != null)
            return false;
        if (httpsTruststoreFile != null ? !httpsTruststoreFile.equals(that.httpsTruststoreFile) : that.httpsTruststoreFile != null)
            return false;
        return !(httpsTruststorePassword != null ? !httpsTruststorePassword.equals(that.httpsTruststorePassword) : that.httpsTruststorePassword != null);

    }

    @Override
    public int hashCode() {
        int result = proxyURL != null ? proxyURL.hashCode() : 0;
        result = 31 * result + (proxyPort != null ? proxyPort.hashCode() : 0);
        result = 31 * result + (proxyUsername != null ? proxyUsername.hashCode() : 0);
        result = 31 * result + (proxyPassword != null ? proxyPassword.hashCode() : 0);
        result = 31 * result + (httpsKeystoreFile != null ? httpsKeystoreFile.hashCode() : 0);
        result = 31 * result + (httpsKeystorePassword != null ? httpsKeystorePassword.hashCode() : 0);
        result = 31 * result + (httpsTruststoreFile != null ? httpsTruststoreFile.hashCode() : 0);
        result = 31 * result + (httpsTruststorePassword != null ? httpsTruststorePassword.hashCode() : 0);
        return result;
    }
}
