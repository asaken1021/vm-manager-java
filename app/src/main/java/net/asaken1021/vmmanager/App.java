package net.asaken1021.vmmanager;

import java.util.HashMap;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        Map<String, String> argsMap = parseArgument(args);
        String uri = "qemu:///system";
        String isoImagesPath = "";

        if (argsMap.containsKey("uri")) {
            if (argsMap.get("uri").equals("")) {
                printError("--uri オプションが渡されましたが，URIの指定がありません．デフォルトを使用します");
            } else {
                uri = argsMap.get("uri");
            }
        }

        if (argsMap.containsKey("iso-images-path")) {
            if (argsMap.get("iso-images-path").equals("")) {
                printError("--iso-images-path オプションが渡されましたが，パスの指定がありません");
            } else {
                isoImagesPath = argsMap.get("iso-images-path");
            }
        }

        if (argsMap.containsKey("web-api")) {
            new WebApiApp(uri, isoImagesPath).run();
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
                case "--iso-images-path":
                    parsedArgs.put("iso-images-path", "");
                    parsedValueDest = "iso-images-path";
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

    private static void printError(String message) {
        System.err.println("エラー: " + message);
    }
}
