/* Created on 17.09.2025 */
package org.javerland.dataanonymizer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Juraj Pacolt
 */
//@formatter:off
@Command(name = "data-anonymizer", mixinStandardHelpOptions = true, version = "0.0.1",
        description = "Data anonymizer tool by JDBC")
//@formatter:on
public class App implements Runnable {

    @Option(names = { "-d",
            "--driver" }, description = "JDBC driver class", defaultValue = "org.postgresql.Driver", required = true)
    private String driver = "org.postgresql.Driver";
    @Option(names = { "-l", "--url" }, description = "URL connection string", required = true)
    private String url = null;
    @Option(names = { "-u", "--username" }, description = "User name", required = false)
    private String username = null;
    @Option(names = { "-p", "--password" }, description = "Password", required = false)
    private String password = null;

    @Override
    public void run() {
        // TODO ...
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }
}
