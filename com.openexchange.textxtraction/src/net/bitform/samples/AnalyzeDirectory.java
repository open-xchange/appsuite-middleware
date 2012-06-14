package net.bitform.samples;

import net.bitform.api.options.AnalyzeOption;
import net.bitform.api.options.Option;
import net.bitform.api.options.ScrubOption;
import net.bitform.api.secure.SecureOptions;
import net.bitform.api.secure.SecureRequest;
import net.bitform.api.secure.SecureResponse;

import java.io.File;
import java.io.IOException;

public class AnalyzeDirectory {

    public static void main(String[] args) {

        /**
         * Check the command line
         */

        File dir = null;

        if (args.length == 1) {
            {
                dir = new File(args[0]);

                if (!dir.exists() || !dir.isDirectory()) {
                    System.out.println("The directory " + dir.toString() + " is not valid");
                    dir = null;
                }
            }

            if (dir == null) {
                System.out.println("Syntax is AnalyzeDirecyory dir where dir is a valid directory.");
                return;
            }

            /**
             * Create and setup the SecureRequest object
             */

            {
                SecureRequest request = new SecureRequest();

                request.setOption(SecureOptions.JustAnalyze, true);

                /**
                 * Run through the files. Note that the SecureRequest objcet
                 * is REUSED for all the file.
                 */

                File[] files = dir.listFiles();

                for (int i = 0; i < files.length; i++) {


                    if (files[i].isFile() && files[i].exists()) {

                        request.setOption(SecureOptions.SourceDocument, files[i]);

                        try {

                            /**
                             * Execute the request
                             */

                            request.execute();

                            /**
                             * Get the response
                             */

                            SecureResponse response = request.getResponse();

                            if (response.getResult(SecureOptions.WasProcessed)) {

                                /**
                                 * Prints all the scrub and analyze targets in the file.
                                 * Note that in a production version a developer would probably
                                 * want to cache the list scrub/analyse targets to improve
                                 * performance.
                                 */

                                System.out.println("The file " + files[i].getName() + " of format " + response.getResult(SecureOptions.SourceFormat).getName() + " contains:");

                                Option[] options = SecureOptions.getInstance().getAllOptions();

                                for (int j = 0; j < options.length; j++) {
                                    if (options[j] instanceof ScrubOption) {
                                        if (response.getResult((ScrubOption) options[j]) == ScrubOption.Reaction.EXISTS)
                                            System.out.println("   " + options[j].getName());
                                    } else if (options[j] instanceof AnalyzeOption) {
                                        if (response.getResult((AnalyzeOption) options[j]) == AnalyzeOption.Reaction.EXISTS)
                                            System.out.println("   " + options[j].getName());
                                    }
                                }

                            } else {
                                System.out.println("The file " + files[i].getName() + " of format " + response.getResult(SecureOptions.SourceFormat).getName() + " could not be processed.");
                            }

                        } catch (IOException e) {
                            System.out.println("Exception in file " + files[i].getName());
                            e.printStackTrace();
                        } catch (RuntimeException e) {
                            System.out.println("Exception in file " + files[i].getName());
                            e.printStackTrace();
                        } catch (OutOfMemoryError e) {
                            System.out.println("Exception in file " + files[i].getName());
                            e.printStackTrace();
                        } catch (StackOverflowError e) {
                            System.out.println("Exception in file " + files[i].getName());
                            e.printStackTrace();
                        }

                    }
                }

                System.out.println("Done");
            }

        }

    }


}
