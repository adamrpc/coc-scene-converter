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
            String content = String.join("\n", Files.readAllLines(new File(args[0]).toPath()));
            final String className = getClassName(content);
            content = content.replaceAll("(case [^ :]+ ?|default ?):", "$1{SWITCH_PLACEHOLDER}");
            content = content.replaceAll("([a-zA-Z]\\s|[(!])([^\\s:]+)\\s*:\\s*[^\\s,;=)]+([\\s,;=)])", "$1$2$3");
            content = content.replaceAll("\\{SWITCH_PLACEHOLDER}", ":");
            final List<String> constants = getPublicConstants(content);
            System.out.println("Class name : " + className);
            System.out.println("Public functions : \n\t" + String.join("\n\t", getPublicFunctions(content)));
            System.out.println("Protected functions : \n\t" + String.join("\n\t", getProtectedFunctions(content)));
            System.out.println("Private functions : \n\t" + String.join("\n\t", getPrivateFunctions(content)));
            System.out.println("Public constants : \n\t" + String.join("\n\t", constants));
            System.out.println("Private constants : \n\t" + String.join("\n\t", getPrivateConstants(content)));
            System.out.println("Public variables : \n\t" + String.join("\n\t", getPublicVariables(content)));
            System.out.println("Protected variables : \n\t" + String.join("\n\t", getProtectedVariables(content)));
            System.out.println("Private variables : \n\t" + String.join("\n\t", getPrivateVariables(content)));
            final String newContent = normalize(content) + "\nreturn " + className + ";";
            Files.write(new File(getClassName(content) + ".js").toPath(), newContent.getBytes());
            System.out.println("Warnings : \n\t" + String.join("\n\t", getWarnings(newContent)));
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
        return content.replaceAll("([^a-zA-Z]\\s|[(!])outputText\\(", "$1EngineCore.outputText(")
                .replaceAll("([^a-zA-Z]\\s|[(!])clearOutput\\(", "$1EngineCore.clearOutput(")
                .replaceAll("([^a-zA-Z]\\s|[(!])doNext\\(", "$1EngineCore.doNext(")
                .replaceAll("([^a-zA-Z]\\s|[(!])menu\\(", "$1EngineCore.menu(")
                .replaceAll("([^a-zA-Z]\\s|[(!])addButton\\(", "$1EngineCore.addButton(")
                .replaceAll("([^a-zA-Z]\\s|[(!])dynStats\\(", "$1EngineCore.dynStats(")
                .replaceAll("([^a-zA-Z]\\s|[(!])choices\\(", "$1EngineCore.choices(")
                .replaceAll("([^a-zA-Z]\\s|[(!])simpleChoices\\(", "$1EngineCore.choices(")
                .replaceAll("([^a-zA-Z]\\s|[(!])HPChange\\(", "$1EngineCore.HPChange(")
                .replaceAll("([^a-zA-Z]\\s|[(!])fatigue\\(", "$1EngineCore.fatigue(")
                .replaceAll("([^a-zA-Z]\\s|[(!])changeFatigue\\(", "$1EngineCore.changeFatigue(")
                .replaceAll("([^a-zA-Z]\\s|[(!])silly\\(", "$1EngineCore.silly(")
                .replaceAll("([^a-zA-Z]\\s|[(!])hideMenus\\(", "$1EngineCore.hideMenus(")
                .replaceAll("([^a-zA-Z]\\s|[(!])rand\\(", "$1Utils.rand(")
                .replaceAll("([^a-zA-Z]\\s|[(!])curry\\(", "$1Utils.curry(")
                .replaceAll("([^a-zA-Z]\\s|[(!])cockDescript\\(", "$1Descriptors.cockDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])multiCockDescriptLight\\(", "$1Descriptors.multiCockDescriptLight(")
                .replaceAll("([^a-zA-Z]\\s|[(!])clitDescript\\(", "$1Descriptors.clitDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])vaginaDescript\\(", "$1Descriptors.vaginaDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])breastDescript\\(", "$1Descriptors.breastDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])nippleDescript\\(", "$1Descriptors.nippleDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])buttDescript\\(", "$1Descriptors.buttDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])sackDescript\\(", "$1Descriptors.sackDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])simpleBallsDescript\\(", "$1Descriptors.simpleBallsDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])assholeDescript\\(", "$1Descriptors.assholeDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])multiCockDescript\\(", "$1Descriptors.multiCockDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])ballsDescriptLight\\(", "$1Descriptors.ballsDescriptLight(")
                .replaceAll("([^a-zA-Z]\\s|[(!])hipDescript\\(", "$1Descriptors.hipDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])allBreastsDescript\\(", "$1Descriptors.allBreastsDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])hairDescript\\(", "$1Descriptors.hairDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])assDescript\\(", "$1Descriptors.assDescript(")
                .replaceAll("([^a-zA-Z]\\s|[(!])sMultiCockDesc\\(", "$1Descriptors.sMultiCockDesc(")
                .replaceAll("([^a-zA-Z]\\s|[(!])SMultiCockDesc\\(", "$1Descriptors.SMultiCockDesc(")
                .replaceAll("([^a-zA-Z]\\s|[(!])allChestDesc\\(", "$1Descriptors.allChestDesc(")
                .replaceAll("([^a-zA-Z]\\s|[(!])GENDER_", "$1AppearanceDefs.GENDER_")
                .replaceAll("([^a-zA-Z]\\s|[(!])SKIN_TYPE_", "$1AppearanceDefs.SKIN_TYPE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])HAIR_", "$1AppearanceDefs.HAIR_")
                .replaceAll("([^a-zA-Z]\\s|[(!])FACE_", "$1AppearanceDefs.FACE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])TONUGE_", "$1AppearanceDefs.TONUGE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])EYES_", "$1AppearanceDefs.EYES_")
                .replaceAll("([^a-zA-Z]\\s|[(!])EARS_", "$1AppearanceDefs.EARS_")
                .replaceAll("([^a-zA-Z]\\s|[(!])HORNS_", "$1AppearanceDefs.HORNS_")
                .replaceAll("([^a-zA-Z]\\s|[(!])ANTENNAE_", "$1AppearanceDefs.ANTENNAE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])ARM_TYPE_", "$1AppearanceDefs.ARM_TYPE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])TAIL_TYPE_", "$1AppearanceDefs.TAIL_TYPE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])BREAST_CUP_", "$1AppearanceDefs.BREAST_CUP_")
                .replaceAll("([^a-zA-Z]\\s|[(!])WING_TYPE_", "$1AppearanceDefs.WING_TYPE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])LOWER_BODY_", "$1AppearanceDefs.LOWER_BODY_")
                .replaceAll("([^a-zA-Z]\\s|[(!])PIERCING_TYPE_", "$1AppearanceDefs.PIERCING_TYPE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])VAGINA_TYPE_", "$1AppearanceDefs.VAGINA_TYPE_")
                .replaceAll("([^a-zA-Z]\\s|[(!])VAGINA_WETNESS_", "$1AppearanceDefs.VAGINA_WETNESS_")
                .replaceAll("([^a-zA-Z]\\s|[(!])VAGINA_LOOSENESS_", "$1AppearanceDefs.VAGINA_LOOSENESS_")
                .replaceAll("([^a-zA-Z]\\s|[(!])ANAL_WETNESS_", "$1AppearanceDefs.ANAL_WETNESS_")
                .replaceAll("([^a-zA-Z]\\s|[(!])ANAL_LOOSENESS_", "$1AppearanceDefs.ANAL_LOOSENESS_")
                .replaceAll("([^a-zA-Z]\\s|[(!])HIP_RATING_", "$1AppearanceDefs.HIP_RATING_")
                .replaceAll("([^a-zA-Z]\\s|[(!])BUTT_RATING_", "$1AppearanceDefs.BUTT_RATING_")
                .replaceAll("([^a-zA-Z]\\s|[(!])camp([\\s.\\[])", "$1CoC.getInstance().scenes.camp$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])telAdre([\\s()])", "$1CoC.getInstance().scenes.telAdre$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])finter([\\s()])", "$1CoC.getInstance().scenes.followerInteractions$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])amilyScene([\\s()])", "$1CoC.getInstance().scenes.amilyScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])amilyFollower([\\s()])", "$1CoC.getInstance().scenes.amilyScene.amilyFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])anemoneScene([\\s()])", "$1CoC.getInstance().scenes.anemoneScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])arianScene([\\s()])", "$1CoC.getInstance().scenes.arianScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])arianFollower([\\s()])", "$1CoC.getInstance().scenes.arianScene.arianFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])ceraphScene([\\s()])", "$1CoC.getInstance().scenes.ceraphScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])ceraphFollowerScene([\\s()])", "$1CoC.getInstance().scenes.ceraphFollowerScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])ceraphIsFollower([\\s()])", "$1CoC.getInstance().scenes.ceraphFollowerScene.ceraphIsFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])emberScene([\\s()])", "$1CoC.getInstance().scenes.emberScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])followerEmber([\\s()])", "$1CoC.getInstance().scenes.emberScene.followerEmber$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])emberMF([\\s()])", "$1CoC.getInstance().scenes.emberScene.emberMF$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])exgartuan([\\s()])", "$1CoC.getInstance().exgartuan$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])helScene([\\s()])", "$1CoC.getInstance().scenes.helScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])helFollower([\\s()])", "$1CoC.getInstance().scenes.helFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])followerHel([\\s()])", "$1CoC.getInstance().scenes.helScene.followerHel$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])helSpawnScene([\\s()])", "$1CoC.getInstance().scenes.helSpawnScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])helPregnant([\\s()])", "$1CoC.getInstance().scenes.helSpawnScene.helPregnant$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])helspawnFollower([\\s()])", "$1CoC.getInstance().scenes.helSpawnScene.helspawnFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])holliScene([\\s()])", "$1CoC.getInstance().scenes.holliScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])isabellaScene([\\s()])", "$1CoC.getInstance().scenes.isabellaScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])isabellaFollowerScene([\\s()])", "$1CoC.getInstance().scenes.isabellaFollowerScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])isabellaFollower([\\s()])", "$1CoC.getInstance().scenes.isabellaFollowerScene.isabellaFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])isabellaAccent([\\s()])", "$1CoC.getInstance().scenes.isabellaFollowerScene.isabellaAccent$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])izmaFollower([\\s()])", "$1CoC.getInstance().scenes.izmaScene.izmaFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])izmaScene([\\s()])", "$1CoC.getInstance().scenes.izmaScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])jojoScene([\\s()])", "$1CoC.getInstance().scenes.jojoScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])monk([\\s()])", "$1CoC.getInstance().monk$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])campCorruptJojo([\\s()])", "$1CoC.getInstance().scenes.jojoScene.campCorruptJojo$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])kihaFollower([\\s()])", "$1CoC.getInstance().scenes.kihaFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])kihaScene([\\s()])", "$1CoC.getInstance().scenes.kihaScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])followerKiha([\\s()])", "$1CoC.getInstance().scenes.kihaFollower.followerKiha$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])latexGirl([\\s()])", "$1CoC.getInstance().scenes.latexGirl$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])latexGooFollower([\\s()])", "$1CoC.getInstance().scenes.latexGirl.latexGooFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])marbleScene([\\s()])", "$1CoC.getInstance().scenes.marbleScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])marblePurification([\\s()])", "$1CoC.getInstance().scenes.marblePurification$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])marbleFollower([\\s()])", "$1CoC.getInstance().scenes.marbleScene.marbleFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])milkSlave([\\s()])", "$1CoC.getInstance().scenes.milkWaifu.milkSlave$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])milkWaifu([\\s()])", "$1CoC.getInstance().scenes.milkWaifu$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])raphael([\\s()])", "$1CoC.getInstance().scenes.raphael$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])RaphaelLikes([\\s()])", "$1CoC.getInstance().scenes.raphael.RaphaelLikes$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])rathazul([\\s()])", "$1CoC.getInstance().scenes.rathazul$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])sheilaScene([\\s()])", "$1CoC.getInstance().scenes.sheilaScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])shouldraFollower([\\s()])", "$1CoC.getInstance().scenes.shouldraFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])shouldraScene([\\s()])", "$1CoC.getInstance().scenes.shouldraScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])followerShouldra([\\s()])", "$1CoC.getInstance().scenes.shouldraFollower.followerShouldra$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])sophieBimbo([\\s()])", "$1CoC.getInstance().scenes.sophieBimbo$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])sophieScene([\\s()])", "$1CoC.getInstance().scenes.sophieScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])sophieFollowerScene([\\s()])", "$1CoC.getInstance().scenes.sophieFollowerScene$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])bimboSophie([\\s()])", "$1CoC.getInstance().scenes.sophieBimbo.bimboSophie$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])sophieFollower([\\s()])", "$1CoC.getInstance().scenes.sophieFollowerScene.sophieFollower$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])urtaLove([\\s()])", "$1CoC.getInstance().scenes.urta.urtaLove$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])urta([\\s()])", "$1CoC.getInstance().scenes.urta$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])urtaPregs([\\s()])", "$1CoC.getInstance().scenes.urtaPregs$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])urtaHeatRut([\\s()])", "$1CoC.getInstance().scenes.urtaHeatRut$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])valeria([\\s()])", "$1CoC.getInstance().scenes.valeria$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])vapula([\\s()])", "$1CoC.getInstance().scenes.vapula$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])vapulaSlave([\\s()])", "$1CoC.getInstance().scenes.vapula.vapulaSlave$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])player([\\s.\\[])", "$1CoC.getInstance().player$2")
                .replaceAll("([^a-zA-Z]\\s|[(!])flags([\\s.\\[])", "$1CoC.getInstance().flags$2");
    }
    private static String normalize(final String content) {
        final String name = getClassName(content);
        final Stream<String> members = Stream.of(getPublicFunctions(content), getProtectedFunctions(content), getPrivateFunctions(content), getPublicVariables(content), getProtectedVariables(content), getPrivateVariables(content)).flatMap(Collection::stream);
        final Stream<String> staticMembers = Stream.of(getPublicConstants(content)).flatMap(Collection::stream);
        String result = content.replaceAll("public function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("protected function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("private function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("public static const ([^\\s]+?)\\s*=", "$1 =")
                .replaceAll("public const ([^\\s]+?)\\s*=", "$1 =")
                .replaceAll("private static const ([^\\s]+?)\\s*=", "var $1 =")
                .replaceAll("\\)\\s*?\\{", ") {")
                .replaceAll("else\\s*?\\{", "else {")
                .replaceAll("\\}\\s*?else\\s", "} else ")
                .replaceAll("\\\\\"", "MY_CUSTOM_ESCAPE_PLACEHOLDER")
                .replaceAll("\"'", "\"\\'")
                .replaceAll(" == ", " === ")
                .replaceAll(" != ", " !== ")
                .replaceAll("\\n\\s*?\\n", "\n");
        do {
            final Matcher matcher = Pattern.compile("(\"[^\"\\n]*?[^\\\\])'([^\"\\n]*?\")", Pattern.DOTALL).matcher(result);
            result = matcher.replaceAll("$1\\\\'$2");
            matcher.reset();
            if(!matcher.find()) {
                break;
            }
        } while(true);
        result = result.replaceAll("\"([^\"]*?)\"", "'$1'")
                .replaceAll("MY_CUSTOM_ESCAPE_PLACEHOLDER", "\"");
        for(final String item : members.collect(Collectors.toList())) {
            result = result.replaceAll("([^a-zA-Z]\\s|[(\\[{!])" + item + "([ ()=><])", "$1this." + item + "$2");
        }
        for(final String item : staticMembers.collect(Collectors.toList())) {
            result = result.replaceAll("([^a-zA-Z]\\s|[(\\[{!])" + item + "([ ()=><])", "$1" + name + "." + item + "$2");
        }
        return prefixGameFunctionCalls(result);
    }
    private static List<String> getProtectedFunctions(final String content) {
        return matchAll(content, "protected function ([^ ]+?) ?\\(");
    }
    private static List<String> getPublicFunctions(final String content) {
        return matchAll(content, "public function ([^ ]+?) ?\\(");
    }
    private static List<String> getPrivateFunctions(final String content) {
        return matchAll(content, "private function ([^ ]+?) ?\\(");
    }
    private static List<String> getPublicConstants(final String content) {
        return Stream.concat(matchAll(content, "public static const ([^\\s;=]+?)[\\s;=]").stream(), matchAll(content, "public const ([^\\s;=]+?)[\\s;=]").stream()).collect(Collectors.toList());
    }
    private static List<String> getPrivateConstants(final String content) {
        return Stream.concat(matchAll(content, "private static const ([^\\s;=]+?)[\\s;=]").stream(), matchAll(content, "private static var ([^\\s;=]+?)[\\s;=]").stream()).collect(Collectors.toList());
    }
    private static List<String> getPublicVariables(final String content) {
        return matchAll(content, "public var ([^\\s;=]+?)[\\s;=]");
    }
    private static List<String> getProtectedVariables(final String content) {
        return matchAll(content, "protected var ([^\\s;=]+?)[\\s;=]");
    }
    private static List<String> getPrivateVariables(final String content) {
        return matchAll(content, "private var ([^\\s;=]+?)[\\s;=]");
    }
    private static List<String> getWarnings(final String content) {
        return matchAll(content, "\n(.*[^\\t{] [a-zA-Z]+ [a-zA-Z]+\\.[a-zA-Z].*)\n");
    }

}
