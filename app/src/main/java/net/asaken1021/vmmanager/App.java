package net.asaken1021.vmmanager;

import java.util.HashMap;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        Map<String, String> argsMap = parseArgument(args);
        String uri;
        String diskImagesPath;
        String isoImagesPath;
        String allowOrigin;

        uri = getArgValue(argsMap, "uri", "qemu:///system", "--uri オプションが渡されましたが，URIの指定がありません．デフォルトを使用します．");
        diskImagesPath = getArgValue(argsMap, "disk-images-path", "", "--disk-images-path オプションが渡されましたが，パスの指定がありません．");
        isoImagesPath = getArgValue(argsMap, "iso-images-path", "", "--iso-images-path オプションが渡されましたが，パスの指定がありません．");
        allowOrigin = getArgValue(argsMap, "allow-origin", "", "--allow-origin オプションが渡されましたが，オリジンの指定がありません．");

        if (argsMap.containsKey("web-api")) {
            new WebApiApp(uri, diskImagesPath, isoImagesPath, allowOrigin).run();
        } else {
            new CliApp(uri).run();
        }
    }

    private static Map<String, String> parseArgument(String[] args) {
        Map<String, String> parsedArgs = new HashMap<String, String>();

        String parsedValueDest = "";

        for (String arg : args) {
            switch (arg) {
                case "--uri":
                    parsedArgs.put("uri", "");
                    parsedValueDest = "uri";
                    break;
                case "--web-api":
                    parsedArgs.put("web-api", "");
                    break;
                case "--disk-images-path":
                    parsedArgs.put("disk-images-path", "");
                    parsedValueDest = "disk-images-path";
                    break;
                case "--iso-images-path":
                    parsedArgs.put("iso-images-path", "");
                    parsedValueDest = "iso-images-path";
                    break;
                case "--allow-origin":
                    parsedArgs.put("allow-origin", "");
                    parsedValueDest = "allow-origin";
                    break;
                default:
                    if (!parsedValueDest.isEmpty()) {
                        parsedArgs.put(parsedValueDest, arg);
                        parsedValueDest = "";
                    }
                    break;
            }
        }

        return parsedArgs;
    }

    private static String getArgValue(Map<String, String> argsMap, String option, String defaultValue,
            String errorMessage) {
        if (argsMap.containsKey(option)) {
            if (argsMap.get(option).equals("")) {
                printError(errorMessage);
            } else {
                return argsMap.get(option);
            }
        }

        return defaultValue;
    }

    private static void printError(String message) {
        System.err.println("エラー: " + message);
    }
}
