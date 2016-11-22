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
    private static String BEFORE_MEMBER = "([()!\\[{},;=<>&|+\\-*/]\\s*)";
    private static String AFTER_MEMBER = "([\\s()=><,;.\\[])";
    private static String AFTER_FUNCTION = "([(])";
    public static void main(String[] args) {
        for(final File file : (new File(".")).listFiles((File dir, String name) -> { return name.toLowerCase().endsWith(".as"); })) {
            try {
                String content = String.join("\n", Files.readAllLines(file.toPath()));
                final String className = getClassName(content);
                content = content.replaceAll("(\\n\\s*//[^\\n]*)", "$1;");
                content = content.replaceAll("(case [^ :]+ ?|default ?|[\\t\\n]//[^\n]*):", "$1{SWITCH_PLACEHOLDER}");
                content = content.replaceAll("(\"[^\"\n]*):([^\"\n]*\")", "$1{SWITCH_PLACEHOLDER}$2");
                content = content.replaceAll("([a-zA-Z]\\s|[(!])([^\\s:\"]+)\\s*:\\s*[^\\s,;=)\\{]+([\\s,;=)\\{])", "$1$2$3");
                content = content.replaceAll("\\{SWITCH_PLACEHOLDER}", ":");
                content = handleAllInlineIf(content);
                content = content.replaceAll("([ -])\\.", "$10.");
                final List<String> constants = getPublicConstants(content);
                System.out.println("Class name : " + className);
                System.out.println("Public functions : \n\t" + String.join("\n\t", getPublicFunctions(content)));
                System.out.println("Protected functions : \n\t" + String.join("\n\t", getProtectedFunctions(content)));
                System.out.println("Private functions : \n\t" + String.join("\n\t", getPrivateFunctions(content)));
                System.out.println("Internal functions : \n\t" + String.join("\n\t", getInternalFunctions(content)));
                System.out.println("Public constants : \n\t" + String.join("\n\t", constants));
                System.out.println("Private constants : \n\t" + String.join("\n\t", getPrivateConstants(content)));
                System.out.println("Public variables : \n\t" + String.join("\n\t", getPublicVariables(content)));
                System.out.println("Protected variables : \n\t" + String.join("\n\t", getProtectedVariables(content)));
                System.out.println("Private variables : \n\t" + String.join("\n\t", getPrivateVariables(content)));
                final String newContent = normalize(content) + "\nreturn " + className + ";";
                Files.write(new File(getClassName(content) + ".js").toPath(), newContent.getBytes());
                System.out.println("Warnings : \n\t" + String.join("\n\t", getWarnings(newContent)));
            } catch (final IOException e) {
                e.printStackTrace();
            }
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
        return content.replaceAll(BEFORE_MEMBER + "outputText" + AFTER_MEMBER, "$1EngineCore.outputText$2")
                .replaceAll(BEFORE_MEMBER + "clearOutput" + AFTER_MEMBER, "$1EngineCore.clearOutput$2")
                .replaceAll(BEFORE_MEMBER + "doNext" + AFTER_MEMBER, "$1EngineCore.doNext$2")
                .replaceAll(BEFORE_MEMBER + "menu" + AFTER_MEMBER, "$1EngineCore.menu$2")
                .replaceAll(BEFORE_MEMBER + "addButton" + AFTER_MEMBER, "$1EngineCore.addButton$2")
                .replaceAll(BEFORE_MEMBER + "dynStats" + AFTER_MEMBER, "$1EngineCore.dynStats$2")
                .replaceAll(BEFORE_MEMBER + "choices" + AFTER_FUNCTION, "$1EngineCore.choices$2")
                .replaceAll(BEFORE_MEMBER + "simpleChoices" + AFTER_MEMBER, "$1EngineCore.choices$2")
                .replaceAll(BEFORE_MEMBER + "HPChange" + AFTER_MEMBER, "$1EngineCore.HPChange$2")
                .replaceAll(BEFORE_MEMBER + "fatigue" + AFTER_MEMBER, "$1EngineCore.fatigue$2")
                .replaceAll(BEFORE_MEMBER + "changeFatigue" + AFTER_MEMBER, "$1EngineCore.changeFatigue$2")
                .replaceAll(BEFORE_MEMBER + "silly" + AFTER_MEMBER, "$1EngineCore.silly$2")
                .replaceAll(BEFORE_MEMBER + "hideMenus" + AFTER_MEMBER, "$1EngineCore.hideMenus$2")
                .replaceAll(BEFORE_MEMBER + "hideUpDown" + AFTER_MEMBER, "$1EngineCore.hideUpDown$2")
                .replaceAll(BEFORE_MEMBER + "spriteSelect" + AFTER_MEMBER, "$1EngineCore.spriteSelect$2")
                .replaceAll(BEFORE_MEMBER + "statScreenRefresh" + AFTER_MEMBER, "$1EngineCore.statScreenRefresh$2")
                .replaceAll(BEFORE_MEMBER + "createCallBackFunction2" + AFTER_MEMBER, "$1EngineCore.createCallBackFunction2$2")
                .replaceAll(BEFORE_MEMBER + "createCallBackFunction" + AFTER_MEMBER, "$1EngineCore.createCallBackFunction$2")
                .replaceAll(BEFORE_MEMBER + "doYesNo" + AFTER_MEMBER, "$1EngineCore.doYesNo$2")
                .replaceAll(BEFORE_MEMBER + "cleanupAfterCombat" + AFTER_MEMBER, "$1Combat.cleanupAfterCombat$2")
                .replaceAll(BEFORE_MEMBER + "startCombat" + AFTER_MEMBER, "$1Combat.startCombat$2")
                .replaceAll(BEFORE_MEMBER + "combatRoundOver" + AFTER_MEMBER, "$1Combat.combatRoundOver$2")
                .replaceAll(BEFORE_MEMBER + "rand" + AFTER_MEMBER, "$1Utils.rand$2")
                .replaceAll(BEFORE_MEMBER + "num2Text" + AFTER_MEMBER, "$1Utils.num2Text$2")
                .replaceAll(BEFORE_MEMBER + "curry" + AFTER_MEMBER, "$1Utils.curry$2")
                .replaceAll(BEFORE_MEMBER + "cockDescript" + AFTER_MEMBER, "$1Descriptors.cockDescript$2")
                .replaceAll(BEFORE_MEMBER + "multiCockDescriptLight" + AFTER_MEMBER, "$1Descriptors.multiCockDescriptLight$2")
                .replaceAll(BEFORE_MEMBER + "clitDescript" + AFTER_MEMBER, "$1Descriptors.clitDescript$2")
                .replaceAll(BEFORE_MEMBER + "vaginaDescript" + AFTER_MEMBER, "$1Descriptors.vaginaDescript$2")
                .replaceAll(BEFORE_MEMBER + "chestDesc" + AFTER_MEMBER, "$1Descriptors.chestDesc$2")
                .replaceAll(BEFORE_MEMBER + "breastDescript" + AFTER_MEMBER, "$1Descriptors.breastDescript$2")
                .replaceAll(BEFORE_MEMBER + "nippleDescript" + AFTER_MEMBER, "$1Descriptors.nippleDescript$2")
                .replaceAll(BEFORE_MEMBER + "buttDescript" + AFTER_MEMBER, "$1Descriptors.buttDescript$2")
                .replaceAll(BEFORE_MEMBER + "sackDescript" + AFTER_MEMBER, "$1Descriptors.sackDescript$2")
                .replaceAll(BEFORE_MEMBER + "simpleBallsDescript" + AFTER_MEMBER, "$1Descriptors.simpleBallsDescript$2")
                .replaceAll(BEFORE_MEMBER + "assholeDescript" + AFTER_MEMBER, "$1Descriptors.assholeDescript$2")
                .replaceAll(BEFORE_MEMBER + "multiCockDescript" + AFTER_MEMBER, "$1Descriptors.multiCockDescript$2")
                .replaceAll(BEFORE_MEMBER + "ballsDescriptLight" + AFTER_MEMBER, "$1Descriptors.ballsDescriptLight$2")
                .replaceAll(BEFORE_MEMBER + "hipDescript" + AFTER_MEMBER, "$1Descriptors.hipDescript$2")
                .replaceAll(BEFORE_MEMBER + "allBreastsDescript" + AFTER_MEMBER, "$1Descriptors.allBreastsDescript$2")
                .replaceAll(BEFORE_MEMBER + "hairDescript" + AFTER_MEMBER, "$1Descriptors.hairDescript$2")
                .replaceAll(BEFORE_MEMBER + "assDescript" + AFTER_MEMBER, "$1Descriptors.assDescript$2")
                .replaceAll(BEFORE_MEMBER + "sMultiCockDesc" + AFTER_MEMBER, "$1Descriptors.sMultiCockDesc$2")
                .replaceAll(BEFORE_MEMBER + "SMultiCockDesc" + AFTER_MEMBER, "$1Descriptors.SMultiCockDesc$2")
                .replaceAll(BEFORE_MEMBER + "allChestDesc" + AFTER_MEMBER, "$1Descriptors.allChestDesc$2")
                .replaceAll(BEFORE_MEMBER + "biggestBreastSizeDescript" + AFTER_MEMBER, "$1Appearance.biggestBreastSizeDescript$2")
                .replaceAll(BEFORE_MEMBER + "assholeOrPussy" + AFTER_MEMBER, "$1Appearance.assholeOrPussy$2")
                .replaceAll(BEFORE_MEMBER + "GENDER_", "$1AppearanceDefs.GENDER_")
                .replaceAll(BEFORE_MEMBER + "SKIN_TYPE_", "$1AppearanceDefs.SKIN_TYPE_")
                .replaceAll(BEFORE_MEMBER + "HAIR_", "$1AppearanceDefs.HAIR_")
                .replaceAll(BEFORE_MEMBER + "FACE_", "$1AppearanceDefs.FACE_")
                .replaceAll(BEFORE_MEMBER + "TONUGE_", "$1AppearanceDefs.TONUGE_")
                .replaceAll(BEFORE_MEMBER + "EYES_", "$1AppearanceDefs.EYES_")
                .replaceAll(BEFORE_MEMBER + "EARS_", "$1AppearanceDefs.EARS_")
                .replaceAll(BEFORE_MEMBER + "HORNS_", "$1AppearanceDefs.HORNS_")
                .replaceAll(BEFORE_MEMBER + "ANTENNAE_", "$1AppearanceDefs.ANTENNAE_")
                .replaceAll(BEFORE_MEMBER + "ARM_TYPE_", "$1AppearanceDefs.ARM_TYPE_")
                .replaceAll(BEFORE_MEMBER + "TAIL_TYPE_", "$1AppearanceDefs.TAIL_TYPE_")
                .replaceAll(BEFORE_MEMBER + "BREAST_CUP_", "$1AppearanceDefs.BREAST_CUP_")
                .replaceAll(BEFORE_MEMBER + "WING_TYPE_", "$1AppearanceDefs.WING_TYPE_")
                .replaceAll(BEFORE_MEMBER + "LOWER_BODY_", "$1AppearanceDefs.LOWER_BODY_")
                .replaceAll(BEFORE_MEMBER + "PIERCING_TYPE_", "$1AppearanceDefs.PIERCING_TYPE_")
                .replaceAll(BEFORE_MEMBER + "VAGINA_TYPE_", "$1AppearanceDefs.VAGINA_TYPE_")
                .replaceAll(BEFORE_MEMBER + "VAGINA_WETNESS_", "$1AppearanceDefs.VAGINA_WETNESS_")
                .replaceAll(BEFORE_MEMBER + "VAGINA_LOOSENESS_", "$1AppearanceDefs.VAGINA_LOOSENESS_")
                .replaceAll(BEFORE_MEMBER + "ANAL_WETNESS_", "$1AppearanceDefs.ANAL_WETNESS_")
                .replaceAll(BEFORE_MEMBER + "ANAL_LOOSENESS_", "$1AppearanceDefs.ANAL_LOOSENESS_")
                .replaceAll(BEFORE_MEMBER + "HIP_RATING_", "$1AppearanceDefs.HIP_RATING_")
                .replaceAll(BEFORE_MEMBER + "BUTT_RATING_", "$1AppearanceDefs.BUTT_RATING_")
                .replaceAll(BEFORE_MEMBER + "camp" + AFTER_MEMBER, "$1CoC.getInstance().scenes.camp$2")
                .replaceAll(BEFORE_MEMBER + "telAdre" + AFTER_MEMBER, "$1CoC.getInstance().scenes.telAdre$2")
                .replaceAll(BEFORE_MEMBER + "finter" + AFTER_MEMBER, "$1CoC.getInstance().scenes.followerInteractions$2")
                .replaceAll(BEFORE_MEMBER + "amilyScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.amilyScene$2")
                .replaceAll(BEFORE_MEMBER + "amilyFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.amilyScene.amilyFollower$2")
                .replaceAll(BEFORE_MEMBER + "anemoneScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.anemoneScene$2")
                .replaceAll(BEFORE_MEMBER + "arianScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.arianScene$2")
                .replaceAll(BEFORE_MEMBER + "arianFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.arianScene.arianFollower$2")
                .replaceAll(BEFORE_MEMBER + "ceraphScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.ceraphScene$2")
                .replaceAll(BEFORE_MEMBER + "ceraphFollowerScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.ceraphFollowerScene$2")
                .replaceAll(BEFORE_MEMBER + "ceraphIsFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.ceraphFollowerScene.ceraphIsFollower$2")
                .replaceAll(BEFORE_MEMBER + "emberScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.emberScene$2")
                .replaceAll(BEFORE_MEMBER + "followerEmber" + AFTER_MEMBER, "$1CoC.getInstance().scenes.emberScene.followerEmber$2")
                .replaceAll(BEFORE_MEMBER + "emberMF" + AFTER_MEMBER, "$1CoC.getInstance().scenes.emberScene.emberMF$2")
                .replaceAll(BEFORE_MEMBER + "exgartuan" + AFTER_MEMBER, "$1CoC.getInstance().exgartuan$2")
                .replaceAll(BEFORE_MEMBER + "helScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.helScene$2")
                .replaceAll(BEFORE_MEMBER + "helFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.helFollower$2")
                .replaceAll(BEFORE_MEMBER + "followerHel" + AFTER_MEMBER, "$1CoC.getInstance().scenes.helScene.followerHel$2")
                .replaceAll(BEFORE_MEMBER + "helSpawnScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.helSpawnScene$2")
                .replaceAll(BEFORE_MEMBER + "helPregnant" + AFTER_MEMBER, "$1CoC.getInstance().scenes.helSpawnScene.helPregnant$2")
                .replaceAll(BEFORE_MEMBER + "helspawnFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.helSpawnScene.helspawnFollower$2")
                .replaceAll(BEFORE_MEMBER + "holliScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.holliScene$2")
                .replaceAll(BEFORE_MEMBER + "isabellaScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.isabellaScene$2")
                .replaceAll(BEFORE_MEMBER + "isabellaFollowerScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.isabellaFollowerScene$2")
                .replaceAll(BEFORE_MEMBER + "isabellaFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.isabellaFollowerScene.isabellaFollower$2")
                .replaceAll(BEFORE_MEMBER + "isabellaAccent" + AFTER_MEMBER, "$1CoC.getInstance().scenes.isabellaFollowerScene.isabellaAccent$2")
                .replaceAll(BEFORE_MEMBER + "izmaFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.izmaScene.izmaFollower$2")
                .replaceAll(BEFORE_MEMBER + "izmaScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.izmaScene$2")
                .replaceAll(BEFORE_MEMBER + "jojoScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.jojoScene$2")
                .replaceAll(BEFORE_MEMBER + "monk" + AFTER_MEMBER, "$1CoC.getInstance().scenes.jojoScene.monk$2")
                .replaceAll(BEFORE_MEMBER + "campCorruptJojo" + AFTER_MEMBER, "$1CoC.getInstance().scenes.jojoScene.campCorruptJojo$2")
                .replaceAll(BEFORE_MEMBER + "kihaFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.kihaFollower$2")
                .replaceAll(BEFORE_MEMBER + "kihaScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.kihaScene$2")
                .replaceAll(BEFORE_MEMBER + "followerKiha" + AFTER_MEMBER, "$1CoC.getInstance().scenes.kihaFollower.followerKiha$2")
                .replaceAll(BEFORE_MEMBER + "latexGirl" + AFTER_MEMBER, "$1CoC.getInstance().scenes.latexGirl$2")
                .replaceAll(BEFORE_MEMBER + "latexGooFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.latexGirl.latexGooFollower$2")
                .replaceAll(BEFORE_MEMBER + "marbleScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.marbleScene$2")
                .replaceAll(BEFORE_MEMBER + "marblePurification" + AFTER_MEMBER, "$1CoC.getInstance().scenes.marblePurification$2")
                .replaceAll(BEFORE_MEMBER + "marbleFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.marbleScene.marbleFollower$2")
                .replaceAll(BEFORE_MEMBER + "milkSlave" + AFTER_MEMBER, "$1CoC.getInstance().scenes.milkWaifu.milkSlave$2")
                .replaceAll(BEFORE_MEMBER + "milkWaifu" + AFTER_MEMBER, "$1CoC.getInstance().scenes.milkWaifu$2")
                .replaceAll(BEFORE_MEMBER + "raphael" + AFTER_MEMBER, "$1CoC.getInstance().scenes.raphael$2")
                .replaceAll(BEFORE_MEMBER + "RaphaelLikes" + AFTER_MEMBER, "$1CoC.getInstance().scenes.raphael.RaphaelLikes$2")
                .replaceAll(BEFORE_MEMBER + "rathazul" + AFTER_MEMBER, "$1CoC.getInstance().scenes.rathazul$2")
                .replaceAll(BEFORE_MEMBER + "sheilaScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.sheilaScene$2")
                .replaceAll(BEFORE_MEMBER + "shouldraFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.shouldraFollower$2")
                .replaceAll(BEFORE_MEMBER + "shouldraScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.shouldraScene$2")
                .replaceAll(BEFORE_MEMBER + "followerShouldra" + AFTER_MEMBER, "$1CoC.getInstance().scenes.shouldraFollower.followerShouldra$2")
                .replaceAll(BEFORE_MEMBER + "sophieBimbo" + AFTER_MEMBER, "$1CoC.getInstance().scenes.sophieBimbo$2")
                .replaceAll(BEFORE_MEMBER + "sophieScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.sophieScene$2")
                .replaceAll(BEFORE_MEMBER + "sophieFollowerScene" + AFTER_MEMBER, "$1CoC.getInstance().scenes.sophieFollowerScene$2")
                .replaceAll(BEFORE_MEMBER + "bimboSophie" + AFTER_MEMBER, "$1CoC.getInstance().scenes.sophieBimbo.bimboSophie$2")
                .replaceAll(BEFORE_MEMBER + "sophieFollower" + AFTER_MEMBER, "$1CoC.getInstance().scenes.sophieFollowerScene.sophieFollower$2")
                .replaceAll(BEFORE_MEMBER + "urtaLove" + AFTER_MEMBER, "$1CoC.getInstance().scenes.urta.urtaLove$2")
                .replaceAll(BEFORE_MEMBER + "urta" + AFTER_MEMBER, "$1CoC.getInstance().scenes.urta$2")
                .replaceAll(BEFORE_MEMBER + "urtaPregs" + AFTER_MEMBER, "$1CoC.getInstance().scenes.urtaPregs$2")
                .replaceAll(BEFORE_MEMBER + "urtaHeatRut" + AFTER_MEMBER, "$1CoC.getInstance().scenes.urtaHeatRut$2")
                .replaceAll(BEFORE_MEMBER + "valeria" + AFTER_MEMBER, "$1CoC.getInstance().scenes.valeria$2")
                .replaceAll(BEFORE_MEMBER + "vapula" + AFTER_MEMBER, "$1CoC.getInstance().scenes.vapula$2")
                .replaceAll(BEFORE_MEMBER + "vapulaSlave" + AFTER_MEMBER, "$1CoC.getInstance().scenes.vapula.vapulaSlave$2")
                .replaceAll(BEFORE_MEMBER + "player" + AFTER_MEMBER, "$1CoC.getInstance().player$2")
                .replaceAll(BEFORE_MEMBER + "monster" + AFTER_MEMBER, "$1CoC.getInstance().monster$2")
                .replaceAll(BEFORE_MEMBER + "inventory" + AFTER_MEMBER, "$1CoC.getInstance().inventory$2")
                .replaceAll(BEFORE_MEMBER + "playerMenu" + AFTER_MEMBER, "$1EventParser.playerMenu$2")
                .replaceAll(BEFORE_MEMBER + "consumables" + AFTER_MEMBER, "$1ConsumableLib$2")
                .replaceAll(BEFORE_MEMBER + "useables" + AFTER_MEMBER, "$1UsableLib$2")
                .replaceAll(BEFORE_MEMBER + "flags" + AFTER_MEMBER, "$1CoC.getInstance().flags$2");
    }
    private static String normalize(final String content) {
        final String name = getClassName(content);
        final Stream<String> members = Stream.of(getPublicFunctions(content), getProtectedFunctions(content), getPrivateFunctions(content), getInternalFunctions(content), getPublicVariables(content), getProtectedVariables(content), getPrivateVariables(content)).flatMap(Collection::stream);
        final Stream<String> staticMembers = Stream.of(getPublicConstants(content)).flatMap(Collection::stream);
        String result = content.replaceAll("public function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("protected function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("private function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
                .replaceAll("internal function ([^ ]+?) ?\\((.*?)\\)[\\s\\S]*?\\{", name + ".prototype.$1 = function($2) {")
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
        result = result.replaceAll("\"([^\"\\n]*?)\"", "'$1'")
                .replaceAll("MY_CUSTOM_ESCAPE_PLACEHOLDER", "\"");
        for(final String item : members.collect(Collectors.toList())) {
            if(!name.equals(item)) {
                result = result.replaceAll(BEFORE_MEMBER + item + AFTER_MEMBER, "$1this." + item + "$2");
            }
        }
        for(final String item : staticMembers.collect(Collectors.toList())) {
            result = result.replaceAll(BEFORE_MEMBER + item + AFTER_MEMBER, "$1" + name + "." + item + "$2");
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
    private static List<String> getInternalFunctions(final String content) {
        return matchAll(content, "internal function ([^ ]+?) ?\\(");
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

    private static String getInlineIfAction(final String content) throws IOException {
        String newContent = content;
        String oldContent;
        final Pattern pattern = Pattern.compile("(\\t(else)?if ?(\\([^\\(\\)]*\\))|\\telse )[^\\n]*");
        Matcher matcher = pattern.matcher(newContent);
        while(!matcher.find()) {
            oldContent = newContent;
            newContent = oldContent.replaceAll("(\\t(else)?if ?\\(.*?)(\\([^\\(\\)]*\\))([^\\n]*)", "$1$4");
            matcher = pattern.matcher(newContent);
            if(newContent.equals(oldContent)) {
                throw new IOException(content);
            }
        }
        return newContent.replaceAll("(\\t(else)?if ?(\\([^\\(\\)]*\\))|\\telse )([^\\n]*)", "$4");
    }

    private static List<String> allInlineIf(final String content) {
        final Pattern pattern = Pattern.compile("(\\t(else)?if ?( ?\\([^\\(\\)]*\\))|\\telse )[^\\n]+");
        final Matcher matcher = pattern.matcher(content);
        final List<String> result = new LinkedList<>();
        while(matcher.find()) {
            result.add(matcher.group(0));
        }
        return result;
    }

    private static String handleAllInlineIf(final String content) {
        String result = content;
        for(final String inlineIf :  allInlineIf(content.replaceAll("(\\selse) (if ?\\()", "$1$2"))) {
            try {
                final String action = getInlineIfAction(inlineIf);
                if(!action.trim().equals("{") && !action.trim().equals("")) {
                    result = result.replaceAll("(\\t((else )?if ?\\(|else )[^\\n]*)(" + action.replaceAll("([\\(\\)\\{\\[+!?\\\\])", "\\\\$1") + ")", "$1{\n\t$4\n}\n");
                }
            }catch(final IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
