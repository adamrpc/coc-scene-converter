package org.adamrpc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        try {
            final String content = String.join("\n", Files.readAllLines(new File(args[0]).toPath()));
            final String className = getClassName(content);
            final List<String> constants = getPublicConstants(content);
            System.out.println("Class name : " + className);
            System.out.println("Public functions : \n\t" + String.join("\n\t", getPublicFunctions(content)));
            System.out.println("Private functions : \n\t" + String.join("\n\t", getPrivateFunctions(content)));
            System.out.println("Public constants : \n\t" + String.join("\n\t", constants));
            System.out.println("Private constants : \n\t" + String.join("\n\t", getPrivateConstants(content)));
            System.out.println("Public variables : \n\t" + String.join("\n\t", getPublicVariables(content)));
            System.out.println("Private variables : \n\t" + String.join("\n\t", getPrivateVariables(content)));
            final StringBuilder suffix = new StringBuilder();
            suffix.append("\nreturn {\n\t" + className + " : " + className);
            for(final String constant : constants) {
                suffix.append(",\n\t" + constant + " : " + constant );
            }
            suffix.append("\n };");
            final String newContent = normalize(content) + suffix;
            Files.write(new File(getClassName(content) + ".js").toPath(), newContent.getBytes());
        } catch(final IOException e) {
            e.printStackTrace();
        }
    }
    private static String getClassName(final String content) {
        final Pattern pattern = Pattern.compile("public class (.+?) ");
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }
    private static List<String> matchAll(final String content, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);
        final List<String> result = new LinkedList<>();
        while(matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
    private static String prefixGameFunctionCalls(final String content) {
        return content.replaceAll("(\\s)outputText\\(", "$1EngineCore.outputText(")
                .replaceAll("(\\s)clearOutput\\(", "$1EngineCore.clearOutput(")
                .replaceAll("(\\s)doNext\\(", "$1BaseContent.doNext(")
                .replaceAll("(\\s)menu\\(", "$1EngineCore.menu(")
                .replaceAll("(\\s)addButton\\(", "$1EngineCore.addButton(")
                .replaceAll("([\\s(])camp([\\s.\\[])", "$1Game.getInstance().scenes.camp$2")
                .replaceAll("([\\s(])player([\\s.\\[])", "$1Game.getInstance().player$2")
                .replaceAll("([\\s(])flags([\\s.\\[])", "$1Game.getInstance().flags$2");
    }
    private static String normalize(final String content) {
        final String name = getClassName(content);
        final Stream<String> members = Stream.of(getPublicFunctions(content), getPrivateFunctions(content), getPublicVariables(content), getPrivateVariables(content)).flatMap(Collection::stream);
        String result = content.replaceAll("public function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("private function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("public static const ([^ ]+?):.*?=", "var $1 =")
                .replaceAll("public const ([^ ]+?):.*?=", "var $1 =")
                .replaceAll("private static const ([^ ]+?):.*?=", "var $1 =")
                .replaceAll("\\)\\s*?\\{", ") {")
                .replaceAll("else\\s*?\\{", "else {")
                .replaceAll("\\}\\s*?else\\s", "} else ")
                .replaceAll("\\\\\"", "MY_CUSTOM_ESCAPE_PLACEHOLDER")
                .replaceAll("\"'", "\"\\'")
                .replaceAll("\\n\\s*?\\n", "\n");
        do {
            final Matcher matcher = Pattern.compile("(\"[^\"]*?[^\\\\])'([^\"]*?\")", Pattern.DOTALL).matcher(result);
            result = matcher.replaceAll("$1\\\\'$2");
            matcher.reset();
            if(!matcher.find()) {
                break;
            }
        } while(true);
        result = result.replaceAll("\"([^\"]*?)\"", "'$1'")
                .replaceAll("MY_CUSTOM_ESCAPE_PLACEHOLDER", "\"");
        for(final String item : members.collect(Collectors.toList())) {
            result = result.replaceAll("([\\s(\\[{])" + item + "([ ()=><])", "$1this." + item + "$2");
        }
        return prefixGameFunctionCalls(result);
    }
    private static List<String> getPublicFunctions(final String content) {
        return matchAll(content, "public function ([^ ]+?) ?\\(");
    }
    private static List<String> getPrivateFunctions(final String content) {
        return matchAll(content, "private function ([^ ]+?) ?\\(");
    }
    private static List<String> getPublicConstants(final String content) {
        return Stream.concat(matchAll(content, "public static const ([^ ]+?):").stream(), matchAll(content, "public const ([^ ]+?):").stream()).collect(Collectors.toList());
    }
    private static List<String> getPrivateConstants(final String content) {
        return matchAll(content, "private static const ([^ ]+?):");
    }
    private static List<String> getPublicVariables(final String content) {
        return matchAll(content, "public var ([^ ]+?):");
    }
    private static List<String> getPrivateVariables(final String content) {
        return matchAll(content, "private var ([^ ]+?):");
    }
}
