#!/bin/bash
cd "/Users/neerajsaini/Documents/VR Research/RefactoringMiner/New Batch"

echo "Analyzing 68 General SE commits with enhanced C# processor..."
echo ""

TOOL_JAR="../build/libs/RM-fat.jar"
CLONED_REPOS="../cloned_repos"
OUTPUT_DIR="General_SE_Failed_to_detect"
SUCCESS=0
FAILED=0


echo "[1/68] naqtn/ProgrammingInVRChat - 2e9ea9101602"
REPO_PATH="$CLONED_REPOS/naqtn_ProgrammingInVRChat"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/naqtn/ProgrammingInVRChat.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 2e9ea9101602acb4bece90ad1637477f6a006f90 -json "$OUTPUT_DIR/ProgrammingInVRChat_2e9ea9101602acb4bece90ad1637477f6a006f90.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[2/68] gpvigano/VRTK-GearVR-Test - 4b750bf40539"
REPO_PATH="$CLONED_REPOS/gpvigano_VRTK-GearVR-Test"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/gpvigano/VRTK-GearVR-Test.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 4b750bf405397ba0e9d533ca11298ba3846e1110 -json "$OUTPUT_DIR/VRTK-GearVR-Test_4b750bf405397ba0e9d533ca11298ba3846e1110.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[3/68] ucsdlib/geiselVR - db16513c7c64"
REPO_PATH="$CLONED_REPOS/ucsdlib_geiselVR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/ucsdlib/geiselVR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" db16513c7c646c27eeb39977716e8c7bd8cedb93 -json "$OUTPUT_DIR/geiselVR_db16513c7c646c27eeb39977716e8c7bd8cedb93.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[4/68] sh-akira/VirtualMotionCapture - 74e4728c4222"
REPO_PATH="$CLONED_REPOS/sh-akira_VirtualMotionCapture"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/sh-akira/VirtualMotionCapture.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 74e4728c422286ec25f166cb64b0df72261f6056 -json "$OUTPUT_DIR/VirtualMotionCapture_74e4728c422286ec25f166cb64b0df72261f6056.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[5/68] BocuD/VRBuildHelper - bc4b0e897687"
REPO_PATH="$CLONED_REPOS/BocuD_VRBuildHelper"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/BocuD/VRBuildHelper.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" bc4b0e89768752e464640d09d6e0df31d3d51902 -json "$OUTPUT_DIR/VRBuildHelper_bc4b0e89768752e464640d09d6e0df31d3d51902.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[6/68] oneVR/VRWorldToolkit - 2ab3c0f18b75"
REPO_PATH="$CLONED_REPOS/oneVR_VRWorldToolkit"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/oneVR/VRWorldToolkit.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 2ab3c0f18b758ac4394e4231fd8a234382662795 -json "$OUTPUT_DIR/VRWorldToolkit_2ab3c0f18b758ac4394e4231fd8a234382662795.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[7/68] kurotu/VRCQuestTools - 3f818f12071f"
REPO_PATH="$CLONED_REPOS/kurotu_VRCQuestTools"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/kurotu/VRCQuestTools.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 3f818f12071f2d1ac35174357f3c6f8cccc58c39 -json "$OUTPUT_DIR/VRCQuestTools_3f818f12071f2d1ac35174357f3c6f8cccc58c39.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[8/68] vitrivr/vitrivr-vr - c97d4ec084a1"
REPO_PATH="$CLONED_REPOS/vitrivr_vitrivr-vr"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/vitrivr/vitrivr-vr.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" c97d4ec084a1b12b52cf21b9b6b044fd3c947a35 -json "$OUTPUT_DIR/vitrivr-vr_c97d4ec084a1b12b52cf21b9b6b044fd3c947a35.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[9/68] BioMotionLab/TUX - 5eef46623198"
REPO_PATH="$CLONED_REPOS/BioMotionLab_TUX"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/BioMotionLab/TUX.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 5eef466231985bd290283e5cffa1dfd67d06fc2f -json "$OUTPUT_DIR/TUX_5eef466231985bd290283e5cffa1dfd67d06fc2f.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[10/68] Unity-Technologies/EditorXR - e3ea64b46364"
REPO_PATH="$CLONED_REPOS/Unity-Technologies_EditorXR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Unity-Technologies/EditorXR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" e3ea64b463643171a40a5474abf47da2ff48e5af -json "$OUTPUT_DIR/EditorXR_e3ea64b463643171a40a5474abf47da2ff48e5af.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[11/68] Saticmotion/Vivista - 33a3639d270d"
REPO_PATH="$CLONED_REPOS/Saticmotion_Vivista"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Saticmotion/Vivista.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 33a3639d270dc27876cd2aad856010895c1a7f92 -json "$OUTPUT_DIR/Vivista_33a3639d270dc27876cd2aad856010895c1a7f92.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[12/68] alvinquach/jpl-trek-vr - 874df9090f70"
REPO_PATH="$CLONED_REPOS/alvinquach_jpl-trek-vr"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/alvinquach/jpl-trek-vr.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 874df9090f7023510b681e6504cc9a997d35bf97 -json "$OUTPUT_DIR/jpl-trek-vr_874df9090f7023510b681e6504cc9a997d35bf97.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[13/68] PKBeam/Edda - 61efa6bc6cfe"
REPO_PATH="$CLONED_REPOS/PKBeam_Edda"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/PKBeam/Edda.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 61efa6bc6cfe9fd7eb0cf1a989ff01bb5af60b3f -json "$OUTPUT_DIR/Edda_61efa6bc6cfe9fd7eb0cf1a989ff01bb5af60b3f.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[14/68] RussellXie7/Unity_Hololens_Dev - 1a17a5778ad5"
REPO_PATH="$CLONED_REPOS/RussellXie7_Unity_Hololens_Dev"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/RussellXie7/Unity_Hololens_Dev.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 1a17a5778ad5d0c63a3143115d57aa772534d42c -json "$OUTPUT_DIR/Unity_Hololens_Dev_1a17a5778ad5d0c63a3143115d57aa772534d42c.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[15/68] gpvigano/VRTK-GearVR-Test - 4ed519f3ca86"
REPO_PATH="$CLONED_REPOS/gpvigano_VRTK-GearVR-Test"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/gpvigano/VRTK-GearVR-Test.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 4ed519f3ca86b0b582d178b573894ee93ba23d08 -json "$OUTPUT_DIR/VRTK-GearVR-Test_4ed519f3ca86b0b582d178b573894ee93ba23d08.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[16/68] BOLL7708/OpenVRNotificationPipe - 2e67b7f61936"
REPO_PATH="$CLONED_REPOS/BOLL7708_OpenVRNotificationPipe"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/BOLL7708/OpenVRNotificationPipe.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 2e67b7f619361c170a56724026f62c32457c735b -json "$OUTPUT_DIR/OpenVRNotificationPipe_2e67b7f619361c170a56724026f62c32457c735b.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[17/68] gpvigano/VRTK-GearVR-Test - 7b0c0646a9a6"
REPO_PATH="$CLONED_REPOS/gpvigano_VRTK-GearVR-Test"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/gpvigano/VRTK-GearVR-Test.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 7b0c0646a9a6249c7c1101a06bae7c51e01d88c3 -json "$OUTPUT_DIR/VRTK-GearVR-Test_7b0c0646a9a6249c7c1101a06bae7c51e01d88c3.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[18/68] concord-consortium/CEASAR - d575f922fd1f"
REPO_PATH="$CLONED_REPOS/concord-consortium_CEASAR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/concord-consortium/CEASAR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" d575f922fd1fcdc77912c628c4be8b823cfddd9c -json "$OUTPUT_DIR/CEASAR_d575f922fd1fcdc77912c628c4be8b823cfddd9c.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[19/68] benaclejames/VRCFaceTracking - 0a56bb2c1acd"
REPO_PATH="$CLONED_REPOS/benaclejames_VRCFaceTracking"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/benaclejames/VRCFaceTracking.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 0a56bb2c1acd8ce290474c2775f433ed17ee8b60 -json "$OUTPUT_DIR/VRCFaceTracking_0a56bb2c1acd8ce290474c2775f433ed17ee8b60.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[20/68] yutakasi634/Coral-iMD-VR - 2c2a74810db1"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 2c2a74810db1b54bc70b8f0ab1201f0b06f3c725 -json "$OUTPUT_DIR/Coral-iMD-VR_2c2a74810db1b54bc70b8f0ab1201f0b06f3c725.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[21/68] VolcanicArts/VRCOSC - 819604043ba5"
REPO_PATH="$CLONED_REPOS/VolcanicArts_VRCOSC"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/VolcanicArts/VRCOSC.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 819604043ba566b986461831f6b4e813260fc23b -json "$OUTPUT_DIR/VRCOSC_819604043ba566b986461831f6b4e813260fc23b.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[22/68] Math0424/SpaceEngineersVR - 75cf112ad738"
REPO_PATH="$CLONED_REPOS/Math0424_SpaceEngineersVR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Math0424/SpaceEngineersVR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 75cf112ad73819ca2d58292db929c6629028b2de -json "$OUTPUT_DIR/SpaceEngineersVR_75cf112ad73819ca2d58292db929c6629028b2de.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[23/68] chrisjz/sm64vr - 4b5ae703c007"
REPO_PATH="$CLONED_REPOS/chrisjz_sm64vr"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/chrisjz/sm64vr.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 4b5ae703c0070ac4503b92055a2f638be6e4482d -json "$OUTPUT_DIR/sm64vr_4b5ae703c0070ac4503b92055a2f638be6e4482d.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[24/68] yutakasi634/Coral-iMD-VR - a7c5620252a3"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" a7c5620252a3921a698cc9b3cd09d4900e623f70 -json "$OUTPUT_DIR/Coral-iMD-VR_a7c5620252a3921a698cc9b3cd09d4900e623f70.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[25/68] pupil-labs/hmd-eyes - 1af940d30ff0"
REPO_PATH="$CLONED_REPOS/pupil-labs_hmd-eyes"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/pupil-labs/hmd-eyes.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 1af940d30ff09480bdd681e70156d3029b3a63ac -json "$OUTPUT_DIR/hmd-eyes_1af940d30ff09480bdd681e70156d3029b3a63ac.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[26/68] xire-/graphulus - 4b93d3212fb6"
REPO_PATH="$CLONED_REPOS/xire-_graphulus"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/xire-/graphulus.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 4b93d3212fb636b05941b09e165dd8a535ae439d -json "$OUTPUT_DIR/graphulus_4b93d3212fb636b05941b09e165dd8a535ae439d.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[27/68] CognitiveVR/cvr-sdk-unity - d42c8790f37f"
REPO_PATH="$CLONED_REPOS/CognitiveVR_cvr-sdk-unity"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/CognitiveVR/cvr-sdk-unity.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" d42c8790f37f7b4ff0db5d51162f54ba16471496 -json "$OUTPUT_DIR/cvr-sdk-unity_d42c8790f37f7b4ff0db5d51162f54ba16471496.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[28/68] Unity-Technologies/EditorXR - 3d37b01c56e3"
REPO_PATH="$CLONED_REPOS/Unity-Technologies_EditorXR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Unity-Technologies/EditorXR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 3d37b01c56e30ddb59410131e2c32fc731cc1bc3 -json "$OUTPUT_DIR/EditorXR_3d37b01c56e30ddb59410131e2c32fc731cc1bc3.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[29/68] ExtendRealityLtd/VRTK - 88cb6b453b40"
REPO_PATH="$CLONED_REPOS/ExtendRealityLtd_VRTK"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/ExtendRealityLtd/VRTK.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 88cb6b453b40c72523c820de225e07b410a6419e -json "$OUTPUT_DIR/VRTK_88cb6b453b40c72523c820de225e07b410a6419e.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[30/68] gpvigano/VRTK-GearVR-Test - 5c637c2f6c6e"
REPO_PATH="$CLONED_REPOS/gpvigano_VRTK-GearVR-Test"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/gpvigano/VRTK-GearVR-Test.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 5c637c2f6c6e6846d589d55f2d343b032416590f -json "$OUTPUT_DIR/VRTK-GearVR-Test_5c637c2f6c6e6846d589d55f2d343b032416590f.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[31/68] microsoft/MixedRealityToolkit-Unity - 91ae7c5f559b"
REPO_PATH="$CLONED_REPOS/microsoft_MixedRealityToolkit-Unity"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/microsoft/MixedRealityToolkit-Unity.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 91ae7c5f559bdd03d10ae105ab2f9d0ca46fe7e0 -json "$OUTPUT_DIR/MixedRealityToolkit-Unity_91ae7c5f559bdd03d10ae105ab2f9d0ca46fe7e0.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[32/68] PKBeam/Edda - 324abc9edcd4"
REPO_PATH="$CLONED_REPOS/PKBeam_Edda"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/PKBeam/Edda.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 324abc9edcd43d7fbe613375f7e8ba61005e900b -json "$OUTPUT_DIR/Edda_324abc9edcd43d7fbe613375f7e8ba61005e900b.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[33/68] BIVROST/360PlayerWindows - 214dc9236c0e"
REPO_PATH="$CLONED_REPOS/BIVROST_360PlayerWindows"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/BIVROST/360PlayerWindows.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 214dc9236c0ea6999dc668046110f84c86a2561d -json "$OUTPUT_DIR/360PlayerWindows_214dc9236c0ea6999dc668046110f84c86a2561d.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[34/68] Unity-Technologies/EditorXR - 868b80bf4c5e"
REPO_PATH="$CLONED_REPOS/Unity-Technologies_EditorXR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Unity-Technologies/EditorXR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 868b80bf4c5ebba9ee9d85a9b55d237484253b3f -json "$OUTPUT_DIR/EditorXR_868b80bf4c5ebba9ee9d85a9b55d237484253b3f.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[35/68] Unity-Technologies/EditorXR - c717fb415b94"
REPO_PATH="$CLONED_REPOS/Unity-Technologies_EditorXR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Unity-Technologies/EditorXR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" c717fb415b94fb74a93e734dc17a26b3adcf18ee -json "$OUTPUT_DIR/EditorXR_c717fb415b94fb74a93e734dc17a26b3adcf18ee.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[36/68] BIVROST/360PlayerWindows - df079b5e0010"
REPO_PATH="$CLONED_REPOS/BIVROST_360PlayerWindows"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/BIVROST/360PlayerWindows.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" df079b5e0010afd12eadcfe8c3f2fcf1c86528d4 -json "$OUTPUT_DIR/360PlayerWindows_df079b5e0010afd12eadcfe8c3f2fcf1c86528d4.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[37/68] tterpi/VRSketchingGeometry - 0f841e577807"
REPO_PATH="$CLONED_REPOS/tterpi_VRSketchingGeometry"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/tterpi/VRSketchingGeometry.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 0f841e577807fcc6a385f9cdb5681443c0fd3a6f -json "$OUTPUT_DIR/VRSketchingGeometry_0f841e577807fcc6a385f9cdb5681443c0fd3a6f.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[38/68] ViveSoftware/ViveInputUtility-Unity - 4726f2a27f7c"
REPO_PATH="$CLONED_REPOS/ViveSoftware_ViveInputUtility-Unity"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/ViveSoftware/ViveInputUtility-Unity.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 4726f2a27f7c705abb7add83ae2bbb56c1ac7444 -json "$OUTPUT_DIR/ViveInputUtility-Unity_4726f2a27f7c705abb7add83ae2bbb56c1ac7444.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[39/68] MatrikMoon/TournamentAssistant - 132d8921aa74"
REPO_PATH="$CLONED_REPOS/MatrikMoon_TournamentAssistant"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/MatrikMoon/TournamentAssistant.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 132d8921aa7431663d4efc8f7fea6e8b6bfa18fc -json "$OUTPUT_DIR/TournamentAssistant_132d8921aa7431663d4efc8f7fea6e8b6bfa18fc.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[40/68] Sauceke/LoveMachine - 1089b8034596"
REPO_PATH="$CLONED_REPOS/Sauceke_LoveMachine"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Sauceke/LoveMachine.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 1089b803459604d6d816da975143122cb30d7fd5 -json "$OUTPUT_DIR/LoveMachine_1089b803459604d6d816da975143122cb30d7fd5.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[41/68] Joshuarox100/VRC-Inventory-Inventor - ddd42c25c407"
REPO_PATH="$CLONED_REPOS/Joshuarox100_VRC-Inventory-Inventor"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/Joshuarox100/VRC-Inventory-Inventor.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" ddd42c25c4072f6d68ec47a9f168d69617fb92fb -json "$OUTPUT_DIR/VRC-Inventory-Inventor_ddd42c25c4072f6d68ec47a9f168d69617fb92fb.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[42/68] microsoft/MixedRealityToolkit-Unity - 707633c7f107"
REPO_PATH="$CLONED_REPOS/microsoft_MixedRealityToolkit-Unity"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/microsoft/MixedRealityToolkit-Unity.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 707633c7f107d6f6eb712892a37e4f390ec89aa1 -json "$OUTPUT_DIR/MixedRealityToolkit-Unity_707633c7f107d6f6eb712892a37e4f390ec89aa1.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[43/68] PfeifferMicha/imhotep - 315b600435cc"
REPO_PATH="$CLONED_REPOS/PfeifferMicha_imhotep"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/PfeifferMicha/imhotep.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 315b600435cc3b92c24b47effbb364e0b94fb6a6 -json "$OUTPUT_DIR/imhotep_315b600435cc3b92c24b47effbb364e0b94fb6a6.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[44/68] yutakasi634/Coral-iMD-VR - 601dd00a8dfa"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 601dd00a8dfa44c08f6b7ef991441c1fde2b2b17 -json "$OUTPUT_DIR/Coral-iMD-VR_601dd00a8dfa44c08f6b7ef991441c1fde2b2b17.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[45/68] PastaTime/DataRepVR - 10114c54abdb"
REPO_PATH="$CLONED_REPOS/PastaTime_DataRepVR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/PastaTime/DataRepVR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 10114c54abdbbe1695144bb077675c1ee76cb4ce -json "$OUTPUT_DIR/DataRepVR_10114c54abdbbe1695144bb077675c1ee76cb4ce.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[46/68] esperecyan/VRMConverterForVRChat - 3f53fe78eaf4"
REPO_PATH="$CLONED_REPOS/esperecyan_VRMConverterForVRChat"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/esperecyan/VRMConverterForVRChat.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 3f53fe78eaf4265161667602d23fb96a8a919ffd -json "$OUTPUT_DIR/VRMConverterForVRChat_3f53fe78eaf4265161667602d23fb96a8a919ffd.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[47/68] yutakasi634/Coral-iMD-VR - e12a78e63a70"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" e12a78e63a7082b314672d65eff0be4ae40cc491 -json "$OUTPUT_DIR/Coral-iMD-VR_e12a78e63a7082b314672d65eff0be4ae40cc491.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[48/68] dantman/elite-vr-cockpit - 20cc6cb299e4"
REPO_PATH="$CLONED_REPOS/dantman_elite-vr-cockpit"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/dantman/elite-vr-cockpit.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 20cc6cb299e4cee78df2b50af8e763809c45c60f -json "$OUTPUT_DIR/elite-vr-cockpit_20cc6cb299e4cee78df2b50af8e763809c45c60f.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[49/68] yutakasi634/Coral-iMD-VR - 369e559a3947"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 369e559a3947ceda0b6024508b562032fe075ad3 -json "$OUTPUT_DIR/Coral-iMD-VR_369e559a3947ceda0b6024508b562032fe075ad3.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[50/68] dakyneko/DakyModsCVR - 148cc0da7857"
REPO_PATH="$CLONED_REPOS/dakyneko_DakyModsCVR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/dakyneko/DakyModsCVR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 148cc0da7857ec5c1c9ce941e2475d91a799f498 -json "$OUTPUT_DIR/DakyModsCVR_148cc0da7857ec5c1c9ce941e2475d91a799f498.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[51/68] halsafar/BeatSaberSongBrowser - 155ac92214ae"
REPO_PATH="$CLONED_REPOS/halsafar_BeatSaberSongBrowser"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/halsafar/BeatSaberSongBrowser.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 155ac92214ae3014dfbff02d32a8a8bb3d9fce7f -json "$OUTPUT_DIR/BeatSaberSongBrowser_155ac92214ae3014dfbff02d32a8a8bb3d9fce7f.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[52/68] ExtendRealityLtd/Zinnia.Unity - fd9a1657e0ed"
REPO_PATH="$CLONED_REPOS/ExtendRealityLtd_Zinnia.Unity"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/ExtendRealityLtd/Zinnia.Unity.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" fd9a1657e0ed09fea3545e56fd87705ff4ea3945 -json "$OUTPUT_DIR/Zinnia.Unity_fd9a1657e0ed09fea3545e56fd87705ff4ea3945.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[53/68] ultraleap/UnityPlugin - 9aa3b06d6a5b"
REPO_PATH="$CLONED_REPOS/ultraleap_UnityPlugin"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/ultraleap/UnityPlugin.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 9aa3b06d6a5b3460bdce23124fddc0951c88acd2 -json "$OUTPUT_DIR/UnityPlugin_9aa3b06d6a5b3460bdce23124fddc0951c88acd2.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[54/68] VRCBilliards/vrcbce - 54dd40391ea9"
REPO_PATH="$CLONED_REPOS/VRCBilliards_vrcbce"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/VRCBilliards/vrcbce.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 54dd40391ea97c5eed7a2415686196b76a416a06 -json "$OUTPUT_DIR/vrcbce_54dd40391ea97c5eed7a2415686196b76a416a06.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[55/68] mgschwan/VRWorkout - 7e36bd5378a1"
REPO_PATH="$CLONED_REPOS/mgschwan_VRWorkout"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/mgschwan/VRWorkout.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 7e36bd5378a13a9f0c26c8d3fd1c87bc403c35c9 -json "$OUTPUT_DIR/VRWorkout_7e36bd5378a13a9f0c26c8d3fd1c87bc403c35c9.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[56/68] ExtendRealityLtd/VRTK - c2c9e08d0f81"
REPO_PATH="$CLONED_REPOS/ExtendRealityLtd_VRTK"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/ExtendRealityLtd/VRTK.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" c2c9e08d0f81a7a8909d70d6cc962d727eaaae33 -json "$OUTPUT_DIR/VRTK_c2c9e08d0f81a7a8909d70d6cc962d727eaaae33.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[57/68] chychen/air_writing - b8760f2ff404"
REPO_PATH="$CLONED_REPOS/chychen_air_writing"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/chychen/air_writing.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" b8760f2ff40490b33732c6a8a23fe6488f9caa35 -json "$OUTPUT_DIR/air_writing_b8760f2ff40490b33732c6a8a23fe6488f9caa35.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[58/68] yutakasi634/Coral-iMD-VR - 29466ddad579"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 29466ddad5792ccced9b77fc7b2e3f9d61ff020a -json "$OUTPUT_DIR/Coral-iMD-VR_29466ddad5792ccced9b77fc7b2e3f9d61ff020a.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[59/68] benaclejames/VRCFaceTracking - 7149a311f2ff"
REPO_PATH="$CLONED_REPOS/benaclejames_VRCFaceTracking"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/benaclejames/VRCFaceTracking.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 7149a311f2ff5b5544f84811717e5ade7e093688 -json "$OUTPUT_DIR/VRCFaceTracking_7149a311f2ff5b5544f84811717e5ade7e093688.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[60/68] deanstein/CinderellaCityProject - 126c92206b04"
REPO_PATH="$CLONED_REPOS/deanstein_CinderellaCityProject"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/deanstein/CinderellaCityProject.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 126c92206b04be3ad1f09f7666264b089b10ee7c -json "$OUTPUT_DIR/CinderellaCityProject_126c92206b04be3ad1f09f7666264b089b10ee7c.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[61/68] benaclejames/VRCFaceTracking - ad6cc0279939"
REPO_PATH="$CLONED_REPOS/benaclejames_VRCFaceTracking"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/benaclejames/VRCFaceTracking.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" ad6cc0279939d82efbbb27e342bc9e05ed93e90a -json "$OUTPUT_DIR/VRCFaceTracking_ad6cc0279939d82efbbb27e342bc9e05ed93e90a.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[62/68] mgschwan/VRWorkout - 521cb040f165"
REPO_PATH="$CLONED_REPOS/mgschwan_VRWorkout"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/mgschwan/VRWorkout.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 521cb040f165534927048e29dacafb55ada49268 -json "$OUTPUT_DIR/VRWorkout_521cb040f165534927048e29dacafb55ada49268.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[63/68] arm0ky0/SteamVR_Toolkit_Unity - 7b0c0646a9a6"
REPO_PATH="$CLONED_REPOS/arm0ky0_SteamVR_Toolkit_Unity"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/arm0ky0/SteamVR_Toolkit_Unity.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 7b0c0646a9a6249c7c1101a06bae7c51e01d88c3 -json "$OUTPUT_DIR/SteamVR_Toolkit_Unity_7b0c0646a9a6249c7c1101a06bae7c51e01d88c3.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[64/68] benaclejames/VRCFaceTracking - 0b1ad01799fb"
REPO_PATH="$CLONED_REPOS/benaclejames_VRCFaceTracking"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/benaclejames/VRCFaceTracking.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 0b1ad01799fbf9c6e1a9d77b91264187b7f10582 -json "$OUTPUT_DIR/VRCFaceTracking_0b1ad01799fbf9c6e1a9d77b91264187b7f10582.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[65/68] ucsdlib/geiselVR - 9f7dde724c8a"
REPO_PATH="$CLONED_REPOS/ucsdlib_geiselVR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/ucsdlib/geiselVR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 9f7dde724c8abd258d881d87a16ba47a26167184 -json "$OUTPUT_DIR/geiselVR_9f7dde724c8abd258d881d87a16ba47a26167184.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[66/68] benaclejames/VRCFaceTracking - 9c7fd6e7c84c"
REPO_PATH="$CLONED_REPOS/benaclejames_VRCFaceTracking"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/benaclejames/VRCFaceTracking.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 9c7fd6e7c84cee0b52b878d0ed0419b6ae2e4944 -json "$OUTPUT_DIR/VRCFaceTracking_9c7fd6e7c84cee0b52b878d0ed0419b6ae2e4944.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[67/68] yutakasi634/Coral-iMD-VR - ce6428b16443"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" ce6428b1644330ffc4f516b900a93a03f5ac8d3c -json "$OUTPUT_DIR/Coral-iMD-VR_ce6428b1644330ffc4f516b900a93a03f5ac8d3c.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo "[68/68] yutakasi634/Coral-iMD-VR - 8db62b16c15b"
REPO_PATH="$CLONED_REPOS/yutakasi634_Coral-iMD-VR"
[ ! -d "$REPO_PATH" ] && git clone "https://github.com/yutakasi634/Coral-iMD-VR.git" "$REPO_PATH" &>/dev/null
java -jar "$TOOL_JAR" -c "$REPO_PATH" 8db62b16c15b8a589c172f4cfbe688dc73d5213e -json "$OUTPUT_DIR/Coral-iMD-VR_8db62b16c15b8a589c172f4cfbe688dc73d5213e.json" &>/dev/null && ((SUCCESS++)) || ((FAILED++))

echo ""
echo "Completed: $SUCCESS/68 successful, $FAILED/68 failed"
