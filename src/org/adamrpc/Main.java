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
            final String newContent = normalize(content) + "\nreturn " + className + ";";
            Files.write(new File(getClassName(content) + ".js").toPath(), newContent.getBytes());
        } catch(final IOException e) {
            e.printStackTrace();
        }
    }
    private static String getClassName(final String content) {
        final Pattern pattern = Pattern.compile("public( final)? class (.+?)[\\s{]");
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(2) : "";
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
                .replaceAll("(\\s)dynStats\\(", "$1EngineCore.dynStats(")
                .replaceAll("(\\s)HPChange\\(", "$1EngineCore.HPChange(")
                .replaceAll("(\\s)fatigue\\(", "$1EngineCore.fatigue(")
                .replaceAll("(\\s)changeFatigue\\(", "$1EngineCore.changeFatigue(")
                .replaceAll("(\\s)silly\\(", "$1EngineCore.silly(")
                .replaceAll("(\\s)rand\\(", "$1Utils.rand(")
                .replaceAll("(\\s)curry\\(", "$1Utils.curry(")
                .replaceAll("(\\s)cockDescript\\(", "$1Descriptors.cockDescript(")
                .replaceAll("(\\s)multiCockDescriptLight\\(", "$1Descriptors.multiCockDescriptLight(")
                .replaceAll("(\\s)clitDescript\\(", "$1Descriptors.clitDescript(")
                .replaceAll("(\\s)vaginaDescript\\(", "$1Descriptors.vaginaDescript(")
                .replaceAll("(\\s)breastDescript\\(", "$1Descriptors.breastDescript(")
                .replaceAll("(\\s)nippleDescript\\(", "$1Descriptors.nippleDescript(")
                .replaceAll("(\\s)buttDescript\\(", "$1Descriptors.buttDescript(")
                .replaceAll("(\\s)sackDescript\\(", "$1Descriptors.sackDescript(")
                .replaceAll("(\\s)simpleBallsDescript\\(", "$1Descriptors.simpleBallsDescript(")
                .replaceAll("(\\s)assholeDescript\\(", "$1Descriptors.assholeDescript(")
                .replaceAll("(\\s)multiCockDescript\\(", "$1Descriptors.multiCockDescript(")
                .replaceAll("(\\s)ballsDescriptLight\\(", "$1Descriptors.ballsDescriptLight(")
                .replaceAll("(\\s)hipDescript\\(", "$1Descriptors.hipDescript(")
                .replaceAll("(\\s)allBreastsDescript\\(", "$1Descriptors.allBreastsDescript(")
                .replaceAll("(\\s)hairDescript\\(", "$1Descriptors.hairDescript(")
                .replaceAll("(\\s)assDescript\\(", "$1Descriptors.assDescript(")
                .replaceAll("(\\s)sMultiCockDesc\\(", "$1Descriptors.sMultiCockDesc(")
                .replaceAll("(\\s)SMultiCockDesc\\(", "$1Descriptors.SMultiCockDesc(")
                .replaceAll("(\\s)allChestDesc\\(", "$1Descriptors.allChestDesc(")
                .replaceAll("(\\s)GENDER_", "$1AppearanceDefs.GENDER_")
                .replaceAll("(\\s)SKIN_TYPE_", "$1AppearanceDefs.SKIN_TYPE_")
                .replaceAll("(\\s)HAIR_", "$1AppearanceDefs.HAIR_")
                .replaceAll("(\\s)FACE_", "$1AppearanceDefs.FACE_")
                .replaceAll("(\\s)TONUGE_", "$1AppearanceDefs.TONUGE_")
                .replaceAll("(\\s)EYES_", "$1AppearanceDefs.EYES_")
                .replaceAll("(\\s)EARS_", "$1AppearanceDefs.EARS_")
                .replaceAll("(\\s)HORNS_", "$1AppearanceDefs.HORNS_")
                .replaceAll("(\\s)ANTENNAE_", "$1AppearanceDefs.ANTENNAE_")
                .replaceAll("(\\s)ARM_TYPE_", "$1AppearanceDefs.ARM_TYPE_")
                .replaceAll("(\\s)TAIL_TYPE_", "$1AppearanceDefs.TAIL_TYPE_")
                .replaceAll("(\\s)BREAST_CUP_", "$1AppearanceDefs.BREAST_CUP_")
                .replaceAll("(\\s)WING_TYPE_", "$1AppearanceDefs.WING_TYPE_")
                .replaceAll("(\\s)LOWER_BODY_", "$1AppearanceDefs.LOWER_BODY_")
                .replaceAll("(\\s)PIERCING_TYPE_", "$1AppearanceDefs.PIERCING_TYPE_")
                .replaceAll("(\\s)VAGINA_TYPE_", "$1AppearanceDefs.VAGINA_TYPE_")
                .replaceAll("(\\s)VAGINA_WETNESS_", "$1AppearanceDefs.VAGINA_WETNESS_")
                .replaceAll("(\\s)VAGINA_LOOSENESS_", "$1AppearanceDefs.VAGINA_LOOSENESS_")
                .replaceAll("(\\s)ANAL_WETNESS_", "$1AppearanceDefs.ANAL_WETNESS_")
                .replaceAll("(\\s)ANAL_LOOSENESS_", "$1AppearanceDefs.ANAL_LOOSENESS_")
                .replaceAll("(\\s)HIP_RATING_", "$1AppearanceDefs.HIP_RATING_")
                .replaceAll("(\\s)BUTT_RATING_", "$1AppearanceDefs.BUTT_RATING_")
                .replaceAll("([\\s(])camp([\\s.\\[])", "$1CoC.getInstance().scenes.camp$2")
                .replaceAll("([\\s(])player([\\s.\\[])", "$1CoC.getInstance().player$2")
                .replaceAll("([\\s(])flags([\\s.\\[])", "$1CoC.getInstance().flags$2");
    }
    private static String normalize(final String content) {
        final String name = getClassName(content);
        final Stream<String> members = Stream.of(getPublicFunctions(content), getPrivateFunctions(content), getPublicVariables(content), getPrivateVariables(content)).flatMap(Collection::stream);
        final Stream<String> staticMembers = Stream.of(getPublicConstants(content)).flatMap(Collection::stream);
        String result = content.replaceAll("public function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("private function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("public static const ([^ ]+?):.*?=", "var $1 =")
                .replaceAll("public const ([^ ]+?):.*?=", "$1 =")
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
        for(final String item : staticMembers.collect(Collectors.toList())) {
            result = result.replaceAll("([\\s(\\[{])" + item + "([ ()=><])", "$1" + name + "." + item + "$2");
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
