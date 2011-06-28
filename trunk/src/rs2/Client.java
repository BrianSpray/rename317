package rs2;

import java.applet.AppletContext;
import java.awt.*;
import java.io.*;
import java.net.*;

import editor.EditorMain;
import org.peterbjornx.pgl2.util.ServerMemoryManager;
import pgle.PglCallClientNode;
import pgle.PglWrapper;
import rt4.Class7_Sub1;


@SuppressWarnings("serial")
public class Client extends GameShell {

    private PglWrapper pglWrapper;
    private int fieldJ;
    private PglCallClientNode renderNode;

    private static String logicGetAmountString(int i) {
        String s = String.valueOf(i);
        for (int k = s.length() - 3; k > 0; k -= 3)
            s = s.substring(0, k) + "," + s.substring(k);
        if (s.length() > 8)
            s = "@gre@" + s.substring(0, s.length() - 8) + " million @whi@(" + s + ")";
        else if (s.length() > 4)
            s = "@cya@" + s.substring(0, s.length() - 4) + "K @whi@(" + s + ")";
        return " " + s;
    }

    public static int TotalRead = 0;

    public static String getFileNameWithoutExtension(String fileName) {
        File tmpFile = new File(fileName);
        tmpFile.getName();
        int whereDot = tmpFile.getName().lastIndexOf('.');
        if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2) {
            return tmpFile.getName().substring(0, whereDot);
        }
        return "";
    }

    private boolean connectserver = false;

    public void preloadModels() {

        File file = new File("../../origcode/src/model/");
        File[] fileArray = file.listFiles();
        for (int y = 0; y < fileArray.length; y++) {
            try {
                String sss = fileArray[y].getName();
                //System.out.println("Parsing model file "+sss);
                byte[] buffer = ReadFile("../../origcode/src/model/" + sss);
                Model.method460(buffer, Integer.parseInt(/*getFileNameWithoutExtension(sss)*/ sss));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public static final byte[] ReadFile(String s) {
        try {
            byte abyte0[];
            File file = new File(s);
            int i = (int) file.length();
            abyte0 = new byte[i];
            DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(s)));
            datainputstream.readFully(abyte0, 0, i);
            datainputstream.close();
            TotalRead++;
            return abyte0;
        } catch (Exception e) {
            System.out.println((new StringBuilder()).append("Read Error: ").append(s).toString());
            return null;
        }
    }

    private void stopMidi() {
        Signlink.midifade = 0;
        Signlink.midi = "stop";
    }

    private void connectServer() {
        if (!connectserver)
            return;//anti retard protection layer

        int j = 5;
        expectedCRCs[8] = 0;
        int k = 0;
        while (expectedCRCs[8] == 0) {
            String s = "Unknown problem";
            drawLoadingText(20, "Connecting to web server");
            try {
                DataInputStream datainputstream = openJagGrabInputStream("crc" + (int) (Math.random() * 99999999D) + "-" + 317);
                Packet class30_sub2_sub2 = new Packet(new byte[40]);
                datainputstream.readFully(class30_sub2_sub2.data, 0, 40);
                datainputstream.close();
                for (int i1 = 0; i1 < 9; i1++)
                    expectedCRCs[i1] = class30_sub2_sub2.g4();

                int j1 = class30_sub2_sub2.g4();
                int k1 = 1234;
                for (int l1 = 0; l1 < 9; l1++)
                    k1 = (k1 << 1) + expectedCRCs[l1];

                if (j1 != k1) {
                    s = "checksum problem";
                    expectedCRCs[8] = 0;
                }
            } catch (EOFException _ex) {
                s = "EOF problem";
                expectedCRCs[8] = 0;
            } catch (IOException _ex) {
                s = "connection problem";
                expectedCRCs[8] = 0;
            } catch (Exception _ex) {
                s = "logic problem";
                expectedCRCs[8] = 0;
                if (!Signlink.reporterror)
                    return;
            }
            if (expectedCRCs[8] == 0) {
                k++;
                for (int l = j; l > 0; l--) {
                    if (k >= 10) {
                        drawLoadingText(10, "Game updated - please reload page");
                        l = 10;
                    } else {
                        drawLoadingText(10, s + " - Will retry in " + l + " secs.");
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception _ex) {
                    }
                }

                j *= 2;
                if (j > 60)
                    j = 60;
                aBoolean872 = !aBoolean872;
            }
        }

    }

    private boolean menuHasAddFriend(int j) {
        if (j < 0)
            return false;
        int k = menuActionID[j];
        if (k >= 2000)
            k -= 2000;
        return k == 337;
    }

    private void drawChatArea() {
        aRSImageProducer_1166.initDrawingArea();
        Rasterizer.lineOffsets = anIntArray1180;
        chatBack.drawImage(0, 0);
        if (messagePromptRaised) {
            boldFont.drawText(0, aString1121, 40, 239);
            boldFont.drawText(128, promptInput + "*", 60, 239);
        } else if (inputDialogState == 1) {
            boldFont.drawText(0, "Enter amount:", 40, 239);
            boldFont.drawText(128, amountOrNameInput + "*", 60, 239);
        } else if (inputDialogState == 2) {
            boldFont.drawText(0, "Enter name:", 40, 239);
            boldFont.drawText(128, amountOrNameInput + "*", 60, 239);
        } else if (aString844 != null) {
            boldFont.drawText(0, aString844, 40, 239);
            boldFont.drawText(128, "Click to continue", 60, 239);
        } else if (backDialogID != -1)
            interface_render(0, 0, 0, RSInterface.interfaceCache[backDialogID]);
        else if (dialogID != -1) {
            interface_render(0, 0, 0, RSInterface.interfaceCache[dialogID]);
        } else {
            RSFont RSFont = plainFont;
            int j = 0;
            Graphics2D.setBounds(77, 0, 463, 0);
            for (int k = 0; k < 100; k++)
                if (chatMessages[k] != null) {
                    int l = chatTypes[k];
                    int i1 = (70 - j * 14) + anInt1089;
                    String s1 = chatNames[k];
                    byte byte0 = 0;
                    if (s1 != null && s1.startsWith("@cr1@")) {
                        s1 = s1.substring(5);
                        byte0 = 1;
                    }
                    if (s1 != null && s1.startsWith("@cr2@")) {
                        s1 = s1.substring(5);
                        byte0 = 2;
                    }
                    if (l == 0) {
                        if (i1 > 0 && i1 < 110)
                            RSFont.drawTextHLeftVMid(0, chatMessages[k], i1, 4);
                        j++;
                    }
                    if ((l == 1 || l == 2) && (l == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s1))) {
                        if (i1 > 0 && i1 < 110) {
                            int j1 = 4;
                            if (byte0 == 1) {
                                modIcons[0].drawImage(j1, i1 - 12);
                                j1 += 14;
                            }
                            if (byte0 == 2) {
                                modIcons[1].drawImage(j1, i1 - 12);
                                j1 += 14;
                            }
                            RSFont.drawTextHLeftVMid(0, s1 + ":", i1, j1);
                            j1 += RSFont.getTextWidth(s1) + 8;
                            RSFont.drawTextHLeftVMid(255, chatMessages[k], i1, j1);
                        }
                        j++;
                    }
                    if ((l == 3 || l == 7) && ui_split_private_chat == 0 && (l == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s1))) {
                        if (i1 > 0 && i1 < 110) {
                            int k1 = 4;
                            RSFont.drawTextHLeftVMid(0, "From", i1, k1);
                            k1 += RSFont.getTextWidth("From ");
                            if (byte0 == 1) {
                                modIcons[0].drawImage(k1, i1 - 12);
                                k1 += 14;
                            }
                            if (byte0 == 2) {
                                modIcons[1].drawImage(k1, i1 - 12);
                                k1 += 14;
                            }
                            RSFont.drawTextHLeftVMid(0, s1 + ":", i1, k1);
                            k1 += RSFont.getTextWidth(s1) + 8;
                            RSFont.drawTextHLeftVMid(0x800000, chatMessages[k], i1, k1);
                        }
                        j++;
                    }
                    if (l == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
                        if (i1 > 0 && i1 < 110)
                            RSFont.drawTextHLeftVMid(0x800080, s1 + " " + chatMessages[k], i1, 4);
                        j++;
                    }
                    if (l == 5 && ui_split_private_chat == 0 && privateChatMode < 2) {
                        if (i1 > 0 && i1 < 110)
                            RSFont.drawTextHLeftVMid(0x800000, chatMessages[k], i1, 4);
                        j++;
                    }
                    if (l == 6 && ui_split_private_chat == 0 && privateChatMode < 2) {
                        if (i1 > 0 && i1 < 110) {
                            RSFont.drawTextHLeftVMid(0, "To " + s1 + ":", i1, 4);
                            RSFont.drawTextHLeftVMid(0x800000, chatMessages[k], i1, 12 + RSFont.getTextWidth("To " + s1));
                        }
                        j++;
                    }
                    if (l == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
                        if (i1 > 0 && i1 < 110)
                            RSFont.drawTextHLeftVMid(0x7e3200, s1 + " " + chatMessages[k], i1, 4);
                        j++;
                    }
                }

            Graphics2D.setDefaultBounds();
            anInt1211 = j * 14 + 7;
            if (anInt1211 < 78)
                anInt1211 = 78;
            renderChatInterface(77, anInt1211 - anInt1089 - 77, 0, 463, anInt1211);
            String s;
            if (session_player != null && session_player.name != null)
                s = session_player.name;
            else
                s = TextClass.fixName(myUsername);
            RSFont.drawTextHLeftVMid(0, s + ":", 90, 4);
            RSFont.drawTextHLeftVMid(255, inputString + "*", 90, 6 + RSFont.getTextWidth(s + ": "));
            Graphics2D.drawHLine(0, 77, 479, 0);
        }
        if (menuOpen && menuScreenArea == 2)
            drawMenu();
        aRSImageProducer_1166.drawGraphics(357, super.graphics, 17);
        gameScreenCanvas.initDrawingArea();
        Rasterizer.lineOffsets = anIntArray1182;
    }

    public void init() {  /*
		network_worldid = Integer.parseInt(getParameter("nodeid"));
		portOff = Integer.parseInt(getParameter("portoff"));
		String s = getParameter("lowmem");
		if(s != null && s.equals("1"))
			setLowMem();
		else
			setHighMem();
		String s1 = getParameter("free");
		isMembers = !(s1 != null && s1.equals("1"));
		initialize(503, 765);
		*/
        network_worldid = 9;
        portOff = 0;
        setHighMem();
        isMembers = true;
        initialize(765, 503);
        //pglWrapper = new PglWrapper();
    }

    public void startRunnable(Runnable runnable, int i) {
        if (i > 10)
            i = 10;
        if (Signlink.mainapp != null) {
            Signlink.startthread(runnable, i);
        } else {
            super.startRunnable(runnable, i);
        }
    }

    public Socket openSocket(int i) throws IOException {
        if (Signlink.mainapp != null)
            return Signlink.opensocket(i);
        else
            return new Socket(server, i);
    }

    private void processMenuClick() {
        if (activeInterfaceType != 0)
            return;
        int j = super.mouseButtonPressed;
        if (spellSelected == 1 && super.clickX >= 516 && super.clickY >= 160 && super.clickX <= 765 && super.clickY <= 205)
            j = 0;
        if (menuOpen) {
            if (j != 1) {
                int k = super.mouseEventX;
                int j1 = super.mouseEventY;
                if (menuScreenArea == 0) {
                    k -= 4;
                    j1 -= 4;
                }
                if (menuScreenArea == 1) {
                    k -= 553;
                    j1 -= 205;
                }
                if (menuScreenArea == 2) {
                    k -= 17;
                    j1 -= 357;
                }
                if (k < menuOffsetX - 10 || k > menuOffsetX + menuWidth + 10 || j1 < menuOffsetY - 10 || j1 > menuOffsetY + anInt952 + 10) {
                    menuOpen = false;
                    if (menuScreenArea == 1)
                        needDrawTabArea = true;
                    if (menuScreenArea == 2)
                        inputTaken = true;
                }
            }
            if (j == 1) {
                int l = menuOffsetX;
                int k1 = menuOffsetY;
                int i2 = menuWidth;
                int k2 = super.clickX;
                int l2 = super.clickY;
                if (menuScreenArea == 0) {
                    k2 -= 4;
                    l2 -= 4;
                }
                if (menuScreenArea == 1) {
                    k2 -= 553;
                    l2 -= 205;
                }
                if (menuScreenArea == 2) {
                    k2 -= 17;
                    l2 -= 357;
                }
                int i3 = -1;
                for (int j3 = 0; j3 < menuActionRow; j3++) {
                    int k3 = k1 + 31 + (menuActionRow - 1 - j3) * 15;
                    if (k2 > l && k2 < l + i2 && l2 > k3 - 13 && l2 < k3 + 3)
                        i3 = j3;
                }

                if (i3 != -1)
                    doAction(i3);
                menuOpen = false;
                if (menuScreenArea == 1)
                    needDrawTabArea = true;
                if (menuScreenArea == 2) {
                    inputTaken = true;
                }
            }
        } else {
            if (j == 1 && menuActionRow > 0) {
                int i1 = menuActionID[menuActionRow - 1];
                if (i1 == 632 || i1 == 78 || i1 == 867 || i1 == 431 || i1 == 53 || i1 == 74 || i1 == 454 || i1 == 539 || i1 == 493 || i1 == 847 || i1 == 447 || i1 == 1125) {
                    int l1 = menuActionCmd2[menuActionRow - 1];
                    int j2 = menuActionCmd3[menuActionRow - 1];
                    RSInterface class9 = RSInterface.interfaceCache[j2];
                    if (class9.aBoolean259 || class9.aBoolean235) {
                        aBoolean1242 = false;
                        anInt989 = 0;
                        anInt1084 = j2;
                        anInt1085 = l1;
                        activeInterfaceType = 2;
                        anInt1087 = super.clickX;
                        anInt1088 = super.clickY;
                        if (RSInterface.interfaceCache[j2].parentID == openInterfaceID)
                            activeInterfaceType = 1;
                        if (RSInterface.interfaceCache[j2].parentID == backDialogID)
                            activeInterfaceType = 3;
                        return;
                    }
                }
            }
            if (j == 1 && (anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
                j = 2;
            if (j == 1 && menuActionRow > 0)
                doAction(menuActionRow - 1);
            if (j == 2 && menuActionRow > 0)
                determineMenuSize();
        }
    }

    private void saveMidi(boolean flag, byte abyte0[]) {
        Signlink.midifade = flag ? 1 : 0;
        Signlink.midisave(abyte0, abyte0.length);
    }

    private void loadRegion() {
        try {
            anInt985 = -1;
            aClass19_1056.clear();
            aClass19_1013.clear();
            Rasterizer.clearTextureCache();
            unlinkMRUNodes();
            sceneGraph.initToNull();
            System.gc();
            for (int i = 0; i < 4; i++)
                tileSettings[i].init();

            for (int l = 0; l < 4; l++) {
                for (int k1 = 0; k1 < 104; k1++) {
                    for (int j2 = 0; j2 < 104; j2++)
                        tileSettingBits[l][k1][j2] = 0;

                }

            }

            MapRegion mapRegion = new MapRegion(104, 104, tileSettingBits, intGroundArray);
            int k2 = terrainData.length;
            stream.p1isaac(0);
            if (!aBoolean1159) {
                for (int i3 = 0; i3 < k2; i3++) {
                    int i4 = (mapCoordinates[i3] >> 8) * 64 - baseX;
                    int k5 = (mapCoordinates[i3] & 0xff) * 64 - baseY;
                    byte abyte0[] = terrainData[i3];
                    if (abyte0 != null)
                        mapRegion.loadTerrain(abyte0, k5, i4, (anInt1069 - 6) * 8, (anInt1070 - 6) * 8, tileSettings);
                }

                for (int j4 = 0; j4 < k2; j4++) {
                    int l5 = (mapCoordinates[j4] >> 8) * 64 - baseX;
                    int k7 = (mapCoordinates[j4] & 0xff) * 64 - baseY;
                    byte abyte2[] = terrainData[j4];
                    if (abyte2 == null && anInt1070 < 800)
                        mapRegion.initMapTables(k7, 64, 64, l5);
                }

                anInt1097++;
                if (anInt1097 > 160) {
                    anInt1097 = 0;
                    stream.p1isaac(238);
                    stream.p1(96);
                }
                stream.p1isaac(0);
                for (int i6 = 0; i6 < k2; i6++) {
                    byte abyte1[] = aByteArrayArray1247[i6];
                    if (abyte1 != null) {
                        int l8 = (mapCoordinates[i6] >> 8) * 64 - baseX;
                        int k9 = (mapCoordinates[i6] & 0xff) * 64 - baseY;
                        mapRegion.loadObjects(l8, tileSettings, k9, sceneGraph, abyte1);
                    }
                }

            }
            if (aBoolean1159) {
                for (int j3 = 0; j3 < 4; j3++) {
                    for (int k4 = 0; k4 < 13; k4++) {
                        for (int j6 = 0; j6 < 13; j6++) {
                            int l7 = anIntArrayArrayArray1129[j3][k4][j6];
                            if (l7 != -1) {
                                int i9 = l7 >> 24 & 3;
                                int l9 = l7 >> 1 & 3;
                                int j10 = l7 >> 14 & 0x3ff;
                                int l10 = l7 >> 3 & 0x7ff;
                                int j11 = (j10 / 8 << 8) + l10 / 8;
                                for (int l11 = 0; l11 < mapCoordinates.length; l11++) {
                                    if (mapCoordinates[l11] != j11 || terrainData[l11] == null)
                                        continue;
                                    mapRegion.loadMapChunk(i9, l9, tileSettings, k4 * 8, (j10 & 7) * 8, terrainData[l11], (l10 & 7) * 8, j3, j6 * 8);
                                    break;
                                }

                            }
                        }

                    }

                }

                for (int l4 = 0; l4 < 13; l4++) {
                    for (int k6 = 0; k6 < 13; k6++) {
                        int i8 = anIntArrayArrayArray1129[0][l4][k6];
                        if (i8 == -1)
                            mapRegion.initMapTables(k6 * 8, 8, 8, l4 * 8);
                    }

                }

                stream.p1isaac(0);
                for (int l6 = 0; l6 < 4; l6++) {
                    for (int j8 = 0; j8 < 13; j8++) {
                        for (int j9 = 0; j9 < 13; j9++) {
                            int i10 = anIntArrayArrayArray1129[l6][j8][j9];
                            if (i10 != -1) {
                                int k10 = i10 >> 24 & 3;
                                int i11 = i10 >> 1 & 3;
                                int k11 = i10 >> 14 & 0x3ff;
                                int i12 = i10 >> 3 & 0x7ff;
                                int j12 = (k11 / 8 << 8) + i12 / 8;
                                for (int k12 = 0; k12 < mapCoordinates.length; k12++) {
                                    if (mapCoordinates[k12] != j12 || aByteArrayArray1247[k12] == null)
                                        continue;
                                    mapRegion.method183(tileSettings, sceneGraph, k10, j8 * 8, (i12 & 7) * 8, l6, aByteArrayArray1247[k12], (k11 & 7) * 8, i11, j9 * 8);
                                    break;
                                }

                            }
                        }

                    }

                }

            }
            stream.p1isaac(0);
            try {
                mapRegion.addTiles(tileSettings, sceneGraph);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pglWrapper != null)
                pglWrapper.loadNewRegion(mapRegion);
            gameScreenCanvas.initDrawingArea();
            stream.p1isaac(0);
            int k3 = MapRegion.setZ;
            if (k3 > plane)
                k3 = plane;
            if (k3 < plane - 1)
                k3 = plane - 1;
            if (lowMem)
                sceneGraph.setHeightLevel(MapRegion.setZ);
            else
                sceneGraph.setHeightLevel(0);
            for (int i5 = 0; i5 < 104; i5++) {
                for (int i7 = 0; i7 < 104; i7++)
                    spawnGroundItem(i5, i7);

            }

            anInt1051++;
            if (anInt1051 > 98) {
                anInt1051 = 0;
                stream.p1isaac(150);
            }
            method63();
        } catch (Exception exception) {
        }
        ObjectDef.memCache1.unlinkAll();
        if (super.gameFrame != null) {
            stream.p1isaac(210);
            stream.p4(0x3f008edd);
        }
        if (lowMem && Signlink.cache_dat != null) {
            int j = onDemandFetcher.getVersionCount(0);
            for (int i1 = 0; i1 < j; i1++) {
                int l1 = onDemandFetcher.getModelIndex(i1);
                if ((l1 & 0x79) == 0)
                    Model.method461(i1);
            }

        }
        System.gc();
        Rasterizer.resetTextures(20);
        onDemandFetcher.method566();
        int k = (anInt1069 - 6) / 8 - 1;
        int j1 = (anInt1069 + 6) / 8 + 1;
        int i2 = (anInt1070 - 6) / 8 - 1;
        int l2 = (anInt1070 + 6) / 8 + 1;
        if (aBoolean1141) {
            k = 49;
            j1 = 50;
            i2 = 49;
            l2 = 50;
        }
        for (int l3 = k; l3 <= j1; l3++) {
            for (int j5 = i2; j5 <= l2; j5++)
                if (l3 == k || l3 == j1 || j5 == i2 || j5 == l2) {
                    int j7 = onDemandFetcher.getMapIndex(0, j5, l3);
                    if (j7 != -1)
                        onDemandFetcher.method560(j7, 3);
                    int k8 = onDemandFetcher.getMapIndex(1, j5, l3);
                    if (k8 != -1)
                        onDemandFetcher.method560(k8, 3);
                }

        }

    }

    private void unlinkMRUNodes() {
        ObjectDef.memCache1.unlinkAll();
        ObjectDef.memCache2.unlinkAll();
        NpcDef.memCache.unlinkAll();
        ItemDef.memCache2.unlinkAll();
        ItemDef.memCache1.unlinkAll();
        Player.memCache.unlinkAll();
        SpotAnim.memCache.unlinkAll();
        Entity.cleanPglPool();
    }

    private void rendedMapScene(int i) {
        int ai[] = minimapImage.myPixels;
        int j = ai.length;
        for (int k = 0; k < j; k++)
            ai[k] = 0;

        for (int l = 1; l < 103; l++) {
            int i1 = 24628 + (103 - l) * 512 * 4;
            for (int k1 = 1; k1 < 103; k1++) {
                if ((tileSettingBits[i][k1][l] & 0x18) == 0)
                    sceneGraph.drawMinimapTile(i, k1, l, ai, i1, 512);
                if (i < 3 && (tileSettingBits[i + 1][k1][l] & 8) != 0)
                    sceneGraph.drawMinimapTile(i + 1, k1, l, ai, i1, 512);
                i1 += 4;
            }

        }

        int j1 = ((238 + (int) (Math.random() * 20D)) - 10 << 16) + ((238 + (int) (Math.random() * 20D)) - 10 << 8) + ((238 + (int) (Math.random() * 20D)) - 10);
        int l1 = (238 + (int) (Math.random() * 20D)) - 10 << 16;
        minimapImage.initDrawingArea();
        for (int i2 = 1; i2 < 103; i2++) {
            for (int j2 = 1; j2 < 103; j2++) {
                if ((tileSettingBits[i][j2][i2] & 0x18) == 0)
                    drawMapScenes(i2, j1, j2, l1, i);
                if (i < 3 && (tileSettingBits[i + 1][j2][i2] & 8) != 0)
                    drawMapScenes(i2, j1, j2, l1, i + 1);
            }

        }

        gameScreenCanvas.initDrawingArea();
        numOfMapMarkers = 0;
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                int i3 = sceneGraph.getGroundDecorationUID(plane, x, y);
                if (i3 != 0) {
                    i3 = i3 >> 14 & 0x7fff;
                    int j3 = ObjectDef.forID(i3).mapFunctionID;
                    if (j3 >= 0) {
                        int k3 = x;
                        int l3 = y;
                        if (j3 != 22 && j3 != 29 && j3 != 34 && j3 != 36 && j3 != 46 && j3 != 47 && j3 != 48) {
                            byte byte0 = 104;
                            byte byte1 = 104;
                            int ai1[][] = tileSettings[plane].clipData;
                            for (int i4 = 0; i4 < 10; i4++) {
                                int j4 = (int) (Math.random() * 4D);
                                if (j4 == 0 && k3 > 0 && k3 > x - 3 && (ai1[k3 - 1][l3] & 0x1280108) == 0)
                                    k3--;
                                if (j4 == 1 && k3 < byte0 - 1 && k3 < x + 3 && (ai1[k3 + 1][l3] & 0x1280180) == 0)
                                    k3++;
                                if (j4 == 2 && l3 > 0 && l3 > y - 3 && (ai1[k3][l3 - 1] & 0x1280102) == 0)
                                    l3--;
                                if (j4 == 3 && l3 < byte1 - 1 && l3 < y + 3 && (ai1[k3][l3 + 1] & 0x1280120) == 0)
                                    l3++;
                            }

                        }
                        markGraphic[numOfMapMarkers] = mapFunctions[j3];
                        markPosX[numOfMapMarkers] = k3;
                        markPosY[numOfMapMarkers] = l3;
                        numOfMapMarkers++;
                    }
                }
            }

        }

    }

    private void spawnGroundItem(int i, int j) {
        Deque class19 = groundArray[plane][i][j];
        if (class19 == null) {
            sceneGraph.removeGroundItemTile(plane, i, j);
            return;
        }
        int k = 0xfa0a1f01;
        Object obj = null;
        for (Item item = (Item) class19.getFront(); item != null; item = (Item) class19.getNext()) {
            ItemDef itemDef = ItemDef.forID(item.ID);
            int l = itemDef.value;
            if (itemDef.stackable)
                l *= item.anInt1559 + 1;
            //	notifyItemSpawn(item, i + baseX, j + baseY);

            if (l > k) {
                k = l;
                obj = item;
            }
        }

        class19.insertFront(((Node) (obj)));
        Object obj1 = null;
        Object obj2 = null;
        for (Item class30_sub2_sub4_sub2_1 = (Item) class19.getFront(); class30_sub2_sub4_sub2_1 != null; class30_sub2_sub4_sub2_1 = (Item) class19.getNext()) {
            if (class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && obj1 == null)
                obj1 = class30_sub2_sub4_sub2_1;
            if (class30_sub2_sub4_sub2_1.ID != ((Item) (obj)).ID && class30_sub2_sub4_sub2_1.ID != ((Item) (obj1)).ID && obj2 == null)
                obj2 = class30_sub2_sub4_sub2_1;
        }

        int i1 = i + (j << 7) + 0x60000000;
        sceneGraph.addGroundItemTile(i, i1, ((Entity) (obj1)), method42(plane, j * 128 + 64, i * 128 + 64), ((Entity) (obj2)), ((Entity) (obj)), plane, j);
    }

    private void renderNPCs(boolean flag)//todo- check this
    {
        for (int j = 0; j < sessionNpcCount; j++) {
            Npc npc = sessionNpcs[sessionNpcList[j]];
            int k = 0x20000000 + (sessionNpcList[j] << 14);
            if (npc == null || !npc.isVisible() || npc.desc.aBoolean93 != flag)
                continue;
            int npcWidth = npc.boundExtentX >> 7;
            int npcHeight = npc.boundExtentY >> 7;
            if (npcWidth < 0 || npcWidth >= 104 || npcHeight < 0 || npcHeight >= 104)
                continue;
            if (npc.boundDim == 1 && (npc.boundExtentX & 0x7f) == 64 && (npc.boundExtentY & 0x7f) == 64) {
                if (anIntArrayArray929[npcWidth][npcHeight] == anInt1265)
                    continue;
                anIntArrayArray929[npcWidth][npcHeight] = anInt1265;
            }
            if (!npc.desc.aBoolean84)
                k += 0x80000000;
            sceneGraph.addEntityA(plane, npc.currentRotation, method42(plane, npc.boundExtentY, npc.boundExtentX), k, npc.boundExtentY, (npc.boundDim - 1) * 64 + 60, npc.boundExtentX, npc, npc.aBoolean1541);
        }
    }

    private boolean replayWave() {
        return Signlink.wavereplay();
    }

    private void loadError() {
        String s = "ondemand";//was a constant parameter
        System.out.println(s);
        try {
            getAppletContext().showDocument(new URL(getCodeBase(), "loaderror_" + s + ".html"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        do
            try {
                Thread.sleep(1000L);
            } catch (Exception _ex) {
            } while (true);
    }

    private void buildInterfaceMenu(int i, RSInterface class9, int k, int l, int i1, int j1) {
        if (class9.type != 0 || class9.children == null || class9.hidden_until_mouseover)
            return;
        if (k < i || i1 < l || k > i + class9.width || i1 > l + class9.height)
            return;
        int k1 = class9.children.length;
        for (int l1 = 0; l1 < k1; l1++) {
            int i2 = class9.childX[l1] + i;
            int j2 = (class9.childY[l1] + l) - j1;
            RSInterface class9_1 = RSInterface.interfaceCache[class9.children[l1]];
            i2 += class9_1.x_offset;
            j2 += class9_1.y_offset;
            if ((class9_1.anInt230 >= 0 || class9_1.colour_conditionfalse_mouseover != 0) && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height)
                if (class9_1.anInt230 >= 0)
                    anInt886 = class9_1.anInt230;
                else
                    anInt886 = class9_1.id;
            if (class9_1.type == 0) {
                buildInterfaceMenu(i2, class9_1, k, j2, i1, class9_1.scrollPosition);
                if (class9_1.scrollMax > class9_1.height)
                    method65(i2 + class9_1.width, class9_1.height, k, i1, class9_1, j2, true, class9_1.scrollMax);
            } else {
                if (class9_1.atActionType == 1 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
                    boolean flag = false;
                    if (class9_1.content_type != 0)
                        flag = buildFriendsListMenu(class9_1);
                    if (!flag) {
                        //System.out.println("1"+class9_1.tooltip + ", " + class9_1.interfaceID);
                        menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
                        menuActionID[menuActionRow] = 315;
                        menuActionCmd3[menuActionRow] = class9_1.id;
                        menuActionRow++;
                    }
                }
                if (class9_1.atActionType == 2 && spellSelected == 0 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
                    String s = class9_1.selectedActionName;
                    if (s.indexOf(" ") != -1)
                        s = s.substring(0, s.indexOf(" "));
                    menuActionName[menuActionRow] = s + " @gre@" + class9_1.spellName;
                    menuActionID[menuActionRow] = 626;
                    menuActionCmd3[menuActionRow] = class9_1.id;
                    menuActionRow++;
                }
                if (class9_1.atActionType == 3 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
                    menuActionName[menuActionRow] = "Close";
                    menuActionID[menuActionRow] = 200;
                    menuActionCmd3[menuActionRow] = class9_1.id;
                    menuActionRow++;
                }
                if (class9_1.atActionType == 4 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
                    //System.out.println("2"+class9_1.tooltip + ", " + class9_1.interfaceID);
                    menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
                    menuActionID[menuActionRow] = 169;
                    menuActionCmd3[menuActionRow] = class9_1.id;
                    menuActionRow++;
                }
                if (class9_1.atActionType == 5 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
                    //System.out.println("3"+class9_1.tooltip + ", " + class9_1.interfaceID);
                    menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
                    menuActionID[menuActionRow] = 646;
                    menuActionCmd3[menuActionRow] = class9_1.id;
                    menuActionRow++;
                }
                if (class9_1.atActionType == 6 && !aBoolean1149 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
                    //System.out.println("4"+class9_1.tooltip + ", " + class9_1.interfaceID);
                    menuActionName[menuActionRow] = class9_1.tooltip + ", " + class9_1.id;
                    menuActionID[menuActionRow] = 679;
                    menuActionCmd3[menuActionRow] = class9_1.id;
                    menuActionRow++;
                }
                if (class9_1.type == 2) {
                    int k2 = 0;
                    for (int l2 = 0; l2 < class9_1.height; l2++) {
                        for (int i3 = 0; i3 < class9_1.width; i3++) {
                            int j3 = i2 + i3 * (32 + class9_1.invSpritePadX);
                            int k3 = j2 + l2 * (32 + class9_1.invSpritePadY);
                            if (k2 < 20) {
                                j3 += class9_1.spritesX[k2];
                                k3 += class9_1.spritesY[k2];
                            }
                            if (k >= j3 && i1 >= k3 && k < j3 + 32 && i1 < k3 + 32) {
                                mouseInvInterfaceIndex = k2;
                                lastActiveInvInterface = class9_1.id;
                                if (class9_1.inv[k2] > 0) {
                                    ItemDef itemDef = ItemDef.forID(class9_1.inv[k2] - 1);
                                    if (itemSelected == 1 && class9_1.isInventoryInterface) {
                                        if (class9_1.id != anInt1284 || k2 != anInt1283) {
                                            menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
                                            menuActionID[menuActionRow] = 870;
                                            menuActionCmd1[menuActionRow] = itemDef.id;
                                            menuActionCmd2[menuActionRow] = k2;
                                            menuActionCmd3[menuActionRow] = class9_1.id;
                                            menuActionRow++;
                                        }
                                    } else if (spellSelected == 1 && class9_1.isInventoryInterface) {
                                        if ((spellUsableOn & 0x10) == 16) {
                                            menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
                                            menuActionID[menuActionRow] = 543;
                                            menuActionCmd1[menuActionRow] = itemDef.id;
                                            menuActionCmd2[menuActionRow] = k2;
                                            menuActionCmd3[menuActionRow] = class9_1.id;
                                            menuActionRow++;
                                        }
                                    } else {
                                        if (class9_1.isInventoryInterface) {
                                            for (int l3 = 4; l3 >= 3; l3--)
                                                if (itemDef.actions != null && itemDef.actions[l3] != null) {
                                                    menuActionName[menuActionRow] = itemDef.actions[l3] + " @lre@" + itemDef.name;
                                                    if (l3 == 3)
                                                        menuActionID[menuActionRow] = 493;
                                                    if (l3 == 4)
                                                        menuActionID[menuActionRow] = 847;
                                                    menuActionCmd1[menuActionRow] = itemDef.id;
                                                    menuActionCmd2[menuActionRow] = k2;
                                                    menuActionCmd3[menuActionRow] = class9_1.id;
                                                    menuActionRow++;
                                                } else if (l3 == 4) {
                                                    menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
                                                    menuActionID[menuActionRow] = 847;
                                                    menuActionCmd1[menuActionRow] = itemDef.id;
                                                    menuActionCmd2[menuActionRow] = k2;
                                                    menuActionCmd3[menuActionRow] = class9_1.id;
                                                    menuActionRow++;
                                                }

                                        }
                                        if (class9_1.usableItemInterface) {
                                            menuActionName[menuActionRow] = "Use @lre@" + itemDef.name;
                                            menuActionID[menuActionRow] = 447;
                                            menuActionCmd1[menuActionRow] = itemDef.id;
                                            menuActionCmd2[menuActionRow] = k2;
                                            menuActionCmd3[menuActionRow] = class9_1.id;
                                            menuActionRow++;
                                        }
                                        if (class9_1.isInventoryInterface && itemDef.actions != null) {
                                            for (int i4 = 2; i4 >= 0; i4--)
                                                if (itemDef.actions[i4] != null) {
                                                    menuActionName[menuActionRow] = itemDef.actions[i4] + " @lre@" + itemDef.name;
                                                    if (i4 == 0)
                                                        menuActionID[menuActionRow] = 74;
                                                    if (i4 == 1)
                                                        menuActionID[menuActionRow] = 454;
                                                    if (i4 == 2)
                                                        menuActionID[menuActionRow] = 539;
                                                    menuActionCmd1[menuActionRow] = itemDef.id;
                                                    menuActionCmd2[menuActionRow] = k2;
                                                    menuActionCmd3[menuActionRow] = class9_1.id;
                                                    menuActionRow++;
                                                }

                                        }
                                        if (class9_1.actions != null) {
                                            for (int j4 = 4; j4 >= 0; j4--)
                                                if (class9_1.actions[j4] != null) {
                                                    menuActionName[menuActionRow] = class9_1.actions[j4] + " @lre@" + itemDef.name;
                                                    if (j4 == 0)
                                                        menuActionID[menuActionRow] = 632;
                                                    if (j4 == 1)
                                                        menuActionID[menuActionRow] = 78;
                                                    if (j4 == 2)
                                                        menuActionID[menuActionRow] = 867;
                                                    if (j4 == 3)
                                                        menuActionID[menuActionRow] = 431;
                                                    if (j4 == 4)
                                                        menuActionID[menuActionRow] = 53;
                                                    menuActionCmd1[menuActionRow] = itemDef.id;
                                                    menuActionCmd2[menuActionRow] = k2;
                                                    menuActionCmd3[menuActionRow] = class9_1.id;
                                                    menuActionRow++;
                                                }

                                        }
                                        menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + " @gre@(@whi@" + (class9_1.inv[k2] - 1) + "@gre@)";
                                        menuActionID[menuActionRow] = 1125;
                                        menuActionCmd1[menuActionRow] = itemDef.id;
                                        menuActionCmd2[menuActionRow] = k2;
                                        menuActionCmd3[menuActionRow] = class9_1.id;
                                        menuActionRow++;
                                    }
                                }
                            }
                            k2++;
                        }

                    }

                }
            }
        }

    }

    private void renderChatInterface(int j, int k, int l, int i1, int j1) {
        scrollBar1.drawImage(i1, l);
        scrollBar2.drawImage(i1, (l + j) - 16);
        Graphics2D.fillRect(i1, l + 16, 16, j - 32, anInt1002);
        int k1 = ((j - 32) * j) / j1;
        if (k1 < 8)
            k1 = 8;
        int l1 = ((j - 32 - k1) * k) / (j1 - j);
        Graphics2D.fillRect(i1, l + 16 + l1, 16, k1, anInt1063);
        Graphics2D.drawVLine(i1, l + 16 + l1, k1, anInt902);
        Graphics2D.drawVLine(i1 + 1, l + 16 + l1, k1, anInt902);
        Graphics2D.drawHLine(i1, l + 16 + l1, 16, anInt902);
        Graphics2D.drawHLine(i1, l + 17 + l1, 16, anInt902);
        Graphics2D.drawVLine(i1 + 15, l + 16 + l1, k1, anInt927);
        Graphics2D.drawVLine(i1 + 14, l + 17 + l1, k1 - 1, anInt927);
        Graphics2D.drawHLine(i1, l + 15 + l1 + k1, 16, anInt927);
        Graphics2D.drawHLine(i1 + 1, l + 14 + l1 + k1, 15, anInt927);
    }

    private void getNpcPos(Packet stream, int i) {
        anInt839 = 0;
        sessionNpcsAwaitingUpdatePtr = 0;
        updateNPCs(stream);
        networkReadNpcSpawnRequests(i, stream);
        readNpcUpdateMask(stream);
        for (int k = 0; k < anInt839; k++) {
            int l = anIntArray840[k];
            if (sessionNpcs[l].time != currentTime) {
                sessionNpcs[l].desc = null;
                sessionNpcs[l] = null;
            }
        }

        if (stream.pos != i) {
            Signlink.reporterror(myUsername + " size mismatch in getnpcpos - pos:" + stream.pos + " psize:" + i);
            throw new RuntimeException("eek");
        }
        for (int i1 = 0; i1 < sessionNpcCount; i1++)
            if (sessionNpcs[sessionNpcList[i1]] == null) {
                Signlink.reporterror(myUsername + " null entry in npc list - pos:" + i1 + " size:" + sessionNpcCount);
                throw new RuntimeException("eek");
            }

    }

    private void processChatModeClick() {
        if (super.mouseButtonPressed == 1) {
            if (super.clickX >= 6 && super.clickX <= 106 && super.clickY >= 467 && super.clickY <= 499) {
                publicChatMode = (publicChatMode + 1) % 4;
                aBoolean1233 = true;
                inputTaken = true;
                stream.p1isaac(95);
                stream.p1(publicChatMode);
                stream.p1(privateChatMode);
                stream.p1(tradeMode);
            }
            if (super.clickX >= 135 && super.clickX <= 235 && super.clickY >= 467 && super.clickY <= 499) {
                privateChatMode = (privateChatMode + 1) % 3;
                aBoolean1233 = true;
                inputTaken = true;
                stream.p1isaac(95);
                stream.p1(publicChatMode);
                stream.p1(privateChatMode);
                stream.p1(tradeMode);
            }
            if (super.clickX >= 273 && super.clickX <= 373 && super.clickY >= 467 && super.clickY <= 499) {
                tradeMode = (tradeMode + 1) % 3;
                aBoolean1233 = true;
                inputTaken = true;
                stream.p1isaac(95);
                stream.p1(publicChatMode);
                stream.p1(privateChatMode);
                stream.p1(tradeMode);
            }
            if (super.clickX >= 412 && super.clickX <= 512 && super.clickY >= 467 && super.clickY <= 499)
                if (openInterfaceID == -1) {
                    clearTopInterfaces();
                    reportAbuseInput = "";
                    report_abuse_mute_player = false;
                    for (int i = 0; i < RSInterface.interfaceCache.length; i++) {
                        if (RSInterface.interfaceCache[i] == null || RSInterface.interfaceCache[i].content_type != 600)
                            continue;
                        reportAbuseInterfaceID = openInterfaceID = RSInterface.interfaceCache[i].parentID;
                        break;
                    }

                } else {
                    pushMessage("Please close the interface you have open before using 'report abuse'", 0, "");
                }
            anInt940++;
            if (anInt940 > 1386) {
                anInt940 = 0;
                stream.p1isaac(165);
                stream.p1(0);
                int j = stream.pos;
                stream.p1(139);
                stream.p1(150);
                stream.p2(32131);
                stream.p1((int) (Math.random() * 256D));
                stream.p2(3250);
                stream.p1(177);
                stream.p2(24859);
                stream.p1(119);
                if ((int) (Math.random() * 2D) == 0)
                    stream.p2(47234);
                if ((int) (Math.random() * 2D) == 0)
                    stream.p1(21);
                stream.psize1(stream.pos - j);
            }
        }
    }

    private void applyConfigChange(int i) {
        int j = SettingUsagePointers.cache[i].usage;
        if (j == 0)
            return;
        int k = sessionSettings[i];
        if (j == 1) {
            if (k == 1)
                Rasterizer.calculatePalette(0.90000000000000002D);
            if (k == 2)
                Rasterizer.calculatePalette(0.80000000000000004D);
            if (k == 3)
                Rasterizer.calculatePalette(0.69999999999999996D);
            if (k == 4)
                Rasterizer.calculatePalette(0.59999999999999998D);
            ItemDef.memCache1.unlinkAll();
            repaintRequested = true;
        }
        if (j == 3) {
            boolean flag1 = musicEnabled;
            if (k == 0) {
                adjustVolume(musicEnabled, 0);
                musicEnabled = true;
            }
            if (k == 1) {
                adjustVolume(musicEnabled, -400);
                musicEnabled = true;
            }
            if (k == 2) {
                adjustVolume(musicEnabled, -800);
                musicEnabled = true;
            }
            if (k == 3) {
                adjustVolume(musicEnabled, -1200);
                musicEnabled = true;
            }
            if (k == 4)
                musicEnabled = false;
            if (musicEnabled != flag1 && !lowMem) {
                if (musicEnabled) {
                    nextSong = currentSong;
                    songChanging = true;
                    onDemandFetcher.method558(2, nextSong);
                } else {
                    stopMidi();
                }
                prevSong = 0;
            }
        }
        if (j == 4) {
            if (k == 0) {
                wave_on = true;
                setWaveVolume(0);
            }
            if (k == 1) {
                wave_on = true;
                setWaveVolume(-400);
            }
            if (k == 2) {
                wave_on = true;
                setWaveVolume(-800);
            }
            if (k == 3) {
                wave_on = true;
                setWaveVolume(-1200);
            }
            if (k == 4)
                wave_on = false;
        }
        if (j == 5)
            anInt1253 = k;
        if (j == 6)
            anInt1249 = k;
        if (j == 8) {
            ui_split_private_chat = k;
            inputTaken = true;
        }
        if (j == 9)
            anInt913 = k;
    }

    private void updateEntities() {
        try {
            int anInt974 = 0;
            for (int j = -1; j < session_player_count + sessionNpcCount; j++) {
                Object obj;
                if (j == -1)
                    obj = session_player;
                else if (j < session_player_count)
                    obj = session_players[session_player_list[j]];
                else
                    obj = sessionNpcs[sessionNpcList[j - session_player_count]];
                if (obj == null || !((Mobile) (obj)).isVisible())
                    continue;
                if (obj instanceof Npc) {
                    NpcDef npcDef = ((Npc) obj).desc;
                    if (npcDef.childrenIDs != null)
                        npcDef = npcDef.method161();
                    if (npcDef == null)
                        continue;
                }
                if (j < session_player_count) {
                    int l = 30;
                    Player player = (Player) obj;
                    if (player.headIcon != 0) {
                        npcScreenPos(((Mobile) (obj)), ((Mobile) (obj)).height + 15);
                        if (spriteDrawX > -1) {
                            for (int i2 = 0; i2 < 8; i2++)
                                if ((player.headIcon & 1 << i2) != 0) {
                                    headIcons[i2].drawSprite(spriteDrawX - 12, spriteDrawY - l);
                                    l -= 25;
                                }

                        }
                    }
                    if (j >= 0 && anInt855 == 10 && anInt933 == session_player_list[j]) {
                        npcScreenPos(((Mobile) (obj)), ((Mobile) (obj)).height + 15);
                        if (spriteDrawX > -1)
                            headIcons[7].drawSprite(spriteDrawX - 12, spriteDrawY - l);
                    }
                } else {
                    NpcDef npcDef = ((Npc) obj).desc;
                    if (npcDef.anInt75 >= 0 && npcDef.anInt75 < headIcons.length) {
                        npcScreenPos(((Mobile) (obj)), ((Mobile) (obj)).height + 15);
                        if (spriteDrawX > -1)
                            headIcons[npcDef.anInt75].drawSprite(spriteDrawX - 12, spriteDrawY - 30);
                    }
                    if (anInt855 == 1 && anInt1222 == sessionNpcList[j - session_player_count] && currentTime % 20 < 10) {
                        npcScreenPos(((Mobile) (obj)), ((Mobile) (obj)).height + 15);
                        if (spriteDrawX > -1)
                            headIcons[2].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
                    }
                }
                if (((Mobile) (obj)).textSpoken != null && (j >= session_player_count || publicChatMode == 0 || publicChatMode == 3 || publicChatMode == 1 && isFriendOrSelf(((Player) obj).name))) {
                    npcScreenPos(((Mobile) (obj)), ((Mobile) (obj)).height);
                    if (spriteDrawX > -1 && anInt974 < anInt975) {
                        anIntArray979[anInt974] = boldFont.method384(((Mobile) (obj)).textSpoken) / 2;
                        anIntArray978[anInt974] = boldFont.charHeight;
                        anIntArray976[anInt974] = spriteDrawX;
                        anIntArray977[anInt974] = spriteDrawY;
                        anIntArray980[anInt974] = ((Mobile) (obj)).anInt1513;
                        anIntArray981[anInt974] = ((Mobile) (obj)).anInt1531;
                        anIntArray982[anInt974] = ((Mobile) (obj)).textCycle;
                        aStringArray983[anInt974++] = ((Mobile) (obj)).textSpoken;
                        if (anInt1249 == 0 && ((Mobile) (obj)).anInt1531 >= 1 && ((Mobile) (obj)).anInt1531 <= 3) {
                            anIntArray978[anInt974] += 10;
                            anIntArray977[anInt974] += 5;
                        }
                        if (anInt1249 == 0 && ((Mobile) (obj)).anInt1531 == 4)
                            anIntArray979[anInt974] = 60;
                        if (anInt1249 == 0 && ((Mobile) (obj)).anInt1531 == 5)
                            anIntArray978[anInt974] += 5;
                    }
                }
                if (((Mobile) (obj)).loopCycleStatus > currentTime) {
                    try {
                        npcScreenPos(((Mobile) (obj)), ((Mobile) (obj)).height + 15);
                        if (spriteDrawX > -1) {
                            int i1 = (((Mobile) (obj)).currentHealth * 30) / ((Mobile) (obj)).maxHealth;
                            if (i1 > 30)
                                i1 = 30;
                            Graphics2D.fillRect(spriteDrawX - 15, spriteDrawY - 3, i1, 5, 65280);
                            Graphics2D.fillRect((spriteDrawX - 15) + i1, spriteDrawY - 3, 30 - i1, 5, 0xff0000);
                        }
                    } catch (Exception e) {
                    }
                }
                for (int j1 = 0; j1 < 4; j1++)
                    if (((Mobile) (obj)).hitsLoopCycle[j1] > currentTime) {
                        npcScreenPos(((Mobile) (obj)), ((Mobile) (obj)).height / 2);
                        if (spriteDrawX > -1) {
                            if (j1 == 1)
                                spriteDrawY -= 20;
                            if (j1 == 2) {
                                spriteDrawX -= 15;
                                spriteDrawY -= 10;
                            }
                            if (j1 == 3) {
                                spriteDrawX += 15;
                                spriteDrawY -= 10;
                            }
                            hitMarks[((Mobile) (obj)).hitMarkTypes[j1]].drawSprite(spriteDrawX - 12, spriteDrawY - 12);
                            smallFont.drawText(0, String.valueOf(((Mobile) (obj)).hitArray[j1]), spriteDrawY + 4, spriteDrawX);
                            smallFont.drawText(0xffffff, String.valueOf(((Mobile) (obj)).hitArray[j1]), spriteDrawY + 3, spriteDrawX - 1);
                        }
                    }

            }
            for (int k = 0; k < anInt974; k++) {
                int k1 = anIntArray976[k];
                int l1 = anIntArray977[k];
                int j2 = anIntArray979[k];
                int k2 = anIntArray978[k];
                boolean flag = true;
                while (flag) {
                    flag = false;
                    for (int l2 = 0; l2 < k; l2++)
                        if (l1 + 2 > anIntArray977[l2] - anIntArray978[l2] && l1 - k2 < anIntArray977[l2] + 2 && k1 - j2 < anIntArray976[l2] + anIntArray979[l2] && k1 + j2 > anIntArray976[l2] - anIntArray979[l2] && anIntArray977[l2] - anIntArray978[l2] < l1) {
                            l1 = anIntArray977[l2] - anIntArray978[l2];
                            flag = true;
                        }

                }
                spriteDrawX = anIntArray976[k];
                spriteDrawY = anIntArray977[k] = l1;
                String s = aStringArray983[k];
                if (anInt1249 == 0) {
                    int i3 = 0xffff00;
                    if (anIntArray980[k] < 6)
                        i3 = anIntArray965[anIntArray980[k]];
                    if (anIntArray980[k] == 6)
                        i3 = anInt1265 % 20 >= 10 ? 0xffff00 : 0xff0000;
                    if (anIntArray980[k] == 7)
                        i3 = anInt1265 % 20 >= 10 ? 65535 : 255;
                    if (anIntArray980[k] == 8)
                        i3 = anInt1265 % 20 >= 10 ? 0x80ff80 : 45056;
                    if (anIntArray980[k] == 9) {
                        int j3 = 150 - anIntArray982[k];
                        if (j3 < 50)
                            i3 = 0xff0000 + 1280 * j3;
                        else if (j3 < 100)
                            i3 = 0xffff00 - 0x50000 * (j3 - 50);
                        else if (j3 < 150)
                            i3 = 65280 + 5 * (j3 - 100);
                    }
                    if (anIntArray980[k] == 10) {
                        int k3 = 150 - anIntArray982[k];
                        if (k3 < 50)
                            i3 = 0xff0000 + 5 * k3;
                        else if (k3 < 100)
                            i3 = 0xff00ff - 0x50000 * (k3 - 50);
                        else if (k3 < 150)
                            i3 = (255 + 0x50000 * (k3 - 100)) - 5 * (k3 - 100);
                    }
                    if (anIntArray980[k] == 11) {
                        int l3 = 150 - anIntArray982[k];
                        if (l3 < 50)
                            i3 = 0xffffff - 0x50005 * l3;
                        else if (l3 < 100)
                            i3 = 65280 + 0x50005 * (l3 - 50);
                        else if (l3 < 150)
                            i3 = 0xffffff - 0x50000 * (l3 - 100);
                    }
                    if (anIntArray981[k] == 0) {
                        boldFont.drawText(0, s, spriteDrawY + 1, spriteDrawX);
                        boldFont.drawText(i3, s, spriteDrawY, spriteDrawX);
                    }
                    if (anIntArray981[k] == 1) {
                        boldFont.method386(0, s, spriteDrawX, anInt1265, spriteDrawY + 1);
                        boldFont.method386(i3, s, spriteDrawX, anInt1265, spriteDrawY);
                    }
                    if (anIntArray981[k] == 2) {
                        boldFont.method387(spriteDrawX, s, anInt1265, spriteDrawY + 1, 0);
                        boldFont.method387(spriteDrawX, s, anInt1265, spriteDrawY, i3);
                    }
                    if (anIntArray981[k] == 3) {
                        boldFont.method388(150 - anIntArray982[k], s, anInt1265, spriteDrawY + 1, spriteDrawX, 0);
                        boldFont.method388(150 - anIntArray982[k], s, anInt1265, spriteDrawY, spriteDrawX, i3);
                    }
                    if (anIntArray981[k] == 4) {
                        int i4 = boldFont.method384(s);
                        int k4 = ((150 - anIntArray982[k]) * (i4 + 100)) / 150;
                        Graphics2D.setBounds(334, spriteDrawX - 50, spriteDrawX + 50, 0);
                        boldFont.drawTextHLeftVMid(0, s, spriteDrawY + 1, (spriteDrawX + 50) - k4);
                        boldFont.drawTextHLeftVMid(i3, s, spriteDrawY, (spriteDrawX + 50) - k4);
                        Graphics2D.setDefaultBounds();
                    }
                    if (anIntArray981[k] == 5) {
                        int j4 = 150 - anIntArray982[k];
                        int l4 = 0;
                        if (j4 < 25)
                            l4 = j4 - 25;
                        else if (j4 > 125)
                            l4 = j4 - 125;
                        Graphics2D.setBounds(spriteDrawY + 5, 0, 512, spriteDrawY - boldFont.charHeight - 1);
                        boldFont.drawText(0, s, spriteDrawY + 1 + l4, spriteDrawX);
                        boldFont.drawText(i3, s, spriteDrawY + l4, spriteDrawX);
                        Graphics2D.setDefaultBounds();
                    }
                } else {
                    boldFont.drawText(0, s, spriteDrawY + 1, spriteDrawX);
                    boldFont.drawText(0xffff00, s, spriteDrawY, spriteDrawX);
                }
            }
        } catch (Exception e) {
        }

    }

    private void delFriend(long l) {
        try {
            if (l == 0L)
                return;
            for (int i = 0; i < user_friends_count; i++) {
                if (friendsListAsLongs[i] != l)
                    continue;
                user_friends_count--;
                needDrawTabArea = true;
                for (int j = i; j < user_friends_count; j++) {
                    user_friends_name_string[j] = user_friends_name_string[j + 1];
                    user_friends_worldid[j] = user_friends_worldid[j + 1];
                    friendsListAsLongs[j] = friendsListAsLongs[j + 1];
                }

                stream.p1isaac(215);
                stream.p8(l);
                break;
            }
        } catch (RuntimeException runtimeexception) {
            Signlink.reporterror("18622, " + false + ", " + l + ", " + runtimeexception.toString());
            throw new RuntimeException();
        }
    }

    private void drawTabArea() {
        tabAreaDrawingTarget.initDrawingArea();
        Rasterizer.lineOffsets = anIntArray1181;
        invBack.drawImage(0, 0);
        if (invOverlayInterfaceID != -1)
            interface_render(0, 0, 0, RSInterface.interfaceCache[invOverlayInterfaceID]);
        else if (tabInterfaceIDs[tabID] != -1)
            interface_render(0, 0, 0, RSInterface.interfaceCache[tabInterfaceIDs[tabID]]);
        if (menuOpen && menuScreenArea == 1)
            drawMenu();
        tabAreaDrawingTarget.drawGraphics(205, super.graphics, 553);
        gameScreenCanvas.initDrawingArea();
        Rasterizer.lineOffsets = anIntArray1182;
    }

    private void method37(int j) {
        if (!lowMem) {
            if (Rasterizer.textureLastUsed[17] >= j) {
                IndexedImage indexedImage = Rasterizer.textureImages[17];
                int k = indexedImage.imgWidth * indexedImage.imgHeight - 1;
                int j1 = indexedImage.imgWidth * anInt945 * 2;
                byte abyte0[] = indexedImage.imgPixels;
                byte abyte3[] = aByteArray912;
                for (int i2 = 0; i2 <= k; i2++)
                    abyte3[i2] = abyte0[i2 - j1 & k];

                indexedImage.imgPixels = abyte3;
                aByteArray912 = abyte0;
                Rasterizer.resetTexture(17);
                anInt854++;
                if (anInt854 > 1235) {
                    anInt854 = 0;
                    stream.p1isaac(226);
                    stream.p1(0);
                    int l2 = stream.pos;
                    stream.p2(58722);
                    stream.p1(240);
                    stream.p2((int) (Math.random() * 65536D));
                    stream.p1((int) (Math.random() * 256D));
                    if ((int) (Math.random() * 2D) == 0)
                        stream.p2(51825);
                    stream.p1((int) (Math.random() * 256D));
                    stream.p2((int) (Math.random() * 65536D));
                    stream.p2(7130);
                    stream.p2((int) (Math.random() * 65536D));
                    stream.p2(61657);
                    stream.psize1(stream.pos - l2);
                }
            }
            if (Rasterizer.textureLastUsed[24] >= j) {
                IndexedImage indexedImage_1 = Rasterizer.textureImages[24];
                int l = indexedImage_1.imgWidth * indexedImage_1.imgHeight - 1;
                int k1 = indexedImage_1.imgWidth * anInt945 * 2;
                byte abyte1[] = indexedImage_1.imgPixels;
                byte abyte4[] = aByteArray912;
                for (int j2 = 0; j2 <= l; j2++)
                    abyte4[j2] = abyte1[j2 - k1 & l];

                indexedImage_1.imgPixels = abyte4;
                aByteArray912 = abyte1;
                Rasterizer.resetTexture(24);
            }
            if (Rasterizer.textureLastUsed[34] >= j) {
                IndexedImage indexedImage_2 = Rasterizer.textureImages[34];
                int i1 = indexedImage_2.imgWidth * indexedImage_2.imgHeight - 1;
                int l1 = indexedImage_2.imgWidth * anInt945 * 2;
                byte abyte2[] = indexedImage_2.imgPixels;
                byte abyte5[] = aByteArray912;
                for (int k2 = 0; k2 <= i1; k2++)
                    abyte5[k2] = abyte2[k2 - l1 & i1];

                indexedImage_2.imgPixels = abyte5;
                aByteArray912 = abyte2;
                Rasterizer.resetTexture(34);
            }
        }
    }

    private void resetSpokenText() {
        for (int i = -1; i < session_player_count; i++) {
            int j;
            if (i == -1)
                j = session_player_idx;
            else
                j = session_player_list[i];
            Player player = session_players[j];
            if (player != null && player.textCycle > 0) {
                player.textCycle--;
                if (player.textCycle == 0)
                    player.textSpoken = null;
            }
        }

        for (int k = 0; k < sessionNpcCount; k++) {
            int l = sessionNpcList[k];
            Npc npc = sessionNpcs[l];
            if (npc != null && npc.textCycle > 0) {
                npc.textCycle--;
                if (npc.textCycle == 0)
                    npc.textSpoken = null;
            }
        }

    }

    private void calcCameraPos() {
        int i = anInt1098 * 128 + 64;
        int j = anInt1099 * 128 + 64;
        int k = method42(plane, j, i) - anInt1100;
        if (xCameraPos < i) {
            xCameraPos += anInt1101 + ((i - xCameraPos) * anInt1102) / 1000;
            if (xCameraPos > i)
                xCameraPos = i;
        }
        if (xCameraPos > i) {
            xCameraPos -= anInt1101 + ((xCameraPos - i) * anInt1102) / 1000;
            if (xCameraPos < i)
                xCameraPos = i;
        }
        if (zCameraPos < k) {
            zCameraPos += anInt1101 + ((k - zCameraPos) * anInt1102) / 1000;
            if (zCameraPos > k)
                zCameraPos = k;
        }
        if (zCameraPos > k) {
            zCameraPos -= anInt1101 + ((zCameraPos - k) * anInt1102) / 1000;
            if (zCameraPos < k)
                zCameraPos = k;
        }
        if (yCameraPos < j) {
            yCameraPos += anInt1101 + ((j - yCameraPos) * anInt1102) / 1000;
            if (yCameraPos > j)
                yCameraPos = j;
        }
        if (yCameraPos > j) {
            yCameraPos -= anInt1101 + ((yCameraPos - j) * anInt1102) / 1000;
            if (yCameraPos < j)
                yCameraPos = j;
        }
        i = anInt995 * 128 + 64;
        j = anInt996 * 128 + 64;
        k = method42(plane, j, i) - anInt997;
        int l = i - xCameraPos;
        int i1 = k - zCameraPos;
        int j1 = j - yCameraPos;
        int k1 = (int) Math.sqrt(l * l + j1 * j1);
        int l1 = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
        int i2 = (int) (Math.atan2(l, j1) * -325.94900000000001D) & 0x7ff;
        if (l1 < 128)
            l1 = 128;
        if (l1 > 383)
            l1 = 383;
        if (yCameraCurve < l1) {
            yCameraCurve += anInt998 + ((l1 - yCameraCurve) * anInt999) / 1000;
            if (yCameraCurve > l1)
                yCameraCurve = l1;
        }
        if (yCameraCurve > l1) {
            yCameraCurve -= anInt998 + ((yCameraCurve - l1) * anInt999) / 1000;
            if (yCameraCurve < l1)
                yCameraCurve = l1;
        }
        int j2 = i2 - xCameraCurve;
        if (j2 > 1024)
            j2 -= 2048;
        if (j2 < -1024)
            j2 += 2048;
        if (j2 > 0) {
            xCameraCurve += anInt998 + (j2 * anInt999) / 1000;
            xCameraCurve &= 0x7ff;
        }
        if (j2 < 0) {
            xCameraCurve -= anInt998 + (-j2 * anInt999) / 1000;
            xCameraCurve &= 0x7ff;
        }
        int k2 = i2 - xCameraCurve;
        if (k2 > 1024)
            k2 -= 2048;
        if (k2 < -1024)
            k2 += 2048;
        if (k2 < 0 && j2 > 0 || k2 > 0 && j2 < 0)
            xCameraCurve = i2;
    }

    private void drawMenu() {
        int i = menuOffsetX;
        int j = menuOffsetY;
        int k = menuWidth;
        int l = anInt952;
        int i1 = 0x5d5447;
        Graphics2D.fillRect(i, j, k, l, i1);
        Graphics2D.fillRect(i + 1, j + 1, k - 2, 16, 0);
        Graphics2D.drawRect(i + 1, j + 18, k - 2, l - 19, 0);
        boldFont.drawTextHLeftVMid(i1, "Choose Option", j + 14, i + 3);
        int j1 = super.mouseEventX;
        int k1 = super.mouseEventY;
        if (menuScreenArea == 0) {
            j1 -= 4;
            k1 -= 4;
        }
        if (menuScreenArea == 1) {
            j1 -= 553;
            k1 -= 205;
        }
        if (menuScreenArea == 2) {
            j1 -= 17;
            k1 -= 357;
        }
        for (int l1 = 0; l1 < menuActionRow; l1++) {
            int i2 = j + 31 + (menuActionRow - 1 - l1) * 15;
            int j2 = 0xffffff;
            if (j1 > i && j1 < i + k && k1 > i2 - 13 && k1 < i2 + 3)
                j2 = 0xffff00;
            boldFont.method389(true, i + 3, j2, menuActionName[l1], i2);
        }

    }

    private void addFriend(long l) {
        try {
            if (l == 0L)
                return;
            if (user_friends_count >= 100 && anInt1046 != 1) {
                pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
                return;
            }
            if (user_friends_count >= 200) {
                pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
                return;
            }
            String s = TextClass.fixName(TextClass.nameForLong(l));
            for (int i = 0; i < user_friends_count; i++)
                if (friendsListAsLongs[i] == l) {
                    pushMessage(s + " is already on your friend list", 0, "");
                    return;
                }
            for (int j = 0; j < user_ignore_count; j++)
                if (user_ignore_names[j] == l) {
                    pushMessage("Please remove " + s + " from your ignore list first", 0, "");
                    return;
                }

            if (s.equals(session_player.name)) {
                return;
            } else {
                user_friends_name_string[user_friends_count] = s;
                friendsListAsLongs[user_friends_count] = l;
                user_friends_worldid[user_friends_count] = 0;
                user_friends_count++;
                needDrawTabArea = true;
                stream.p1isaac(188);
                stream.p8(l);
                return;
            }
        } catch (RuntimeException runtimeexception) {
            Signlink.reporterror("15283, " + (byte) 68 + ", " + l + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    private int method42(int i, int j, int k) {
        int l = k >> 7;
        int i1 = j >> 7;
        if (l < 0 || i1 < 0 || l > 103 || i1 > 103)
            return 0;
        int j1 = i;
        if (j1 < 3 && (tileSettingBits[1][l][i1] & 2) == 2)
            j1++;
        int k1 = k & 0x7f;
        int l1 = j & 0x7f;
        int i2 = intGroundArray[j1][l][i1] * (128 - k1) + intGroundArray[j1][l + 1][i1] * k1 >> 7;
        int j2 = intGroundArray[j1][l][i1 + 1] * (128 - k1) + intGroundArray[j1][l + 1][i1 + 1] * k1 >> 7;
        return i2 * (128 - l1) + j2 * l1 >> 7;
    }

    private static String logic_get_amount_string(int j) {
        if (j < 0x186a0)
            return String.valueOf(j);
        if (j < 0x989680)
            return j / 1000 + "K";
        else
            return j / 0xf4240 + "M";
    }

    private void network_disconnect() {
        try {
            if (socketStream != null)
                socketStream.close();
        } catch (Exception _ex) {
        }
        socketStream = null;
        loggedIn = false;
        loginScreenState = 0;
        myUsername = "";
        myPassword = "";
        unlinkMRUNodes();
        sceneGraph.initToNull();
        for (int i = 0; i < 4; i++)
            tileSettings[i].init();

        System.gc();
        stopMidi();
        currentSong = -1;
        nextSong = -1;
        prevSong = 0;
    }

    private void char_edit_change_gender() {
        char_edit_model_changed = true;
        for (int type = 0; type < 7; type++) {
            char_edit_idkits[type] = -1;
            for (int idkit_ptr = 0; idkit_ptr < IdentityKit.length; idkit_ptr++) {
                if (IdentityKit.cache[idkit_ptr].notSelectable || IdentityKit.cache[idkit_ptr].bodyPartID != type + (char_edit_gender ? 0 : 7))
                    continue;
                char_edit_idkits[type] = idkit_ptr;
                break;
            }

        }

    }

    private void networkReadNpcSpawnRequests(int i, Packet stream) {
        while (stream.bit_pos + 21 < i * 8) {
            int k = stream.gbits(14);
            if (k == 16383)
                break;
            if (sessionNpcs[k] == null)
                sessionNpcs[k] = new Npc();
            Npc npc = sessionNpcs[k];
            sessionNpcList[sessionNpcCount++] = k;
            npc.time = currentTime;
            int l = stream.gbits(5);
            if (l > 15)
                l -= 32;
            int i1 = stream.gbits(5);
            if (i1 > 15)
                i1 -= 32;
            int j1 = stream.gbits(1);
            npc.desc = NpcDef.forID(stream.gbits(12));
            int k1 = stream.gbits(1);
            if (k1 == 1)
                session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = k;
            npc.boundDim = npc.desc.aByte68;
            npc.anInt1504 = npc.desc.anInt79;
            npc.anInt1554 = npc.desc.anInt67;
            npc.anInt1555 = npc.desc.anInt58;
            npc.anInt1556 = npc.desc.anInt83;
            npc.anInt1557 = npc.desc.anInt55;
            npc.anInt1511 = npc.desc.idleAnimation;
            npc.setPos(session_player.pathX[0] + i1, session_player.pathY[0] + l, j1 == 1);
        }
        stream.end_bit_block();
    }

    public void doLogic() {
        if (rsAlreadyLoaded || loadingError || genericLoadingError)
            return;
        currentTime++;
        if (!loggedIn)
            login_screen_process();
        else
            client_process();
        on_demand_process();
    }

    private void world_post_players_to_renderer(boolean flag) {
        if (session_player.boundExtentX >> 7 == destX && session_player.boundExtentY >> 7 == destY)
            destX = 0;
        int j = session_player_count;
        if (flag)
            j = 1;
        for (int l = 0; l < j; l++) {
            Player player;
            int i1;
            if (flag) {
                player = session_player;
                i1 = session_player_idx << 14;
            } else {
                player = session_players[session_player_list[l]];
                i1 = session_player_list[l] << 14;
            }
            if (player == null || !player.isVisible())
                continue;
            player.aBoolean1699 = (lowMem && session_player_count > 50 || session_player_count > 200) && !flag && player.anInt1517 == player.anInt1511;
            int j1 = player.boundExtentX >> 7;
            int k1 = player.boundExtentY >> 7;
            if (j1 < 0 || j1 >= 104 || k1 < 0 || k1 >= 104)
                continue;
            if (player.aModel_1714 != null && currentTime >= player.anInt1707 && currentTime < player.anInt1708) {
                player.aBoolean1699 = false;
                player.anInt1709 = method42(plane, player.boundExtentY, player.boundExtentX);
                sceneGraph.addEntity(plane, player.boundExtentY, player, player.currentRotation, player.anInt1722, player.boundExtentX, player.anInt1709, player.anInt1719, player.anInt1721, i1, player.anInt1720);
                continue;
            }
            if ((player.boundExtentX & 0x7f) == 64 && (player.boundExtentY & 0x7f) == 64) {
                if (anIntArrayArray929[j1][k1] == anInt1265)
                    continue;
                anIntArrayArray929[j1][k1] = anInt1265;
            }
            player.anInt1709 = method42(plane, player.boundExtentY, player.boundExtentX);
            sceneGraph.addEntityA(plane, player.currentRotation, player.anInt1709, i1, player.boundExtentY, 60, player.boundExtentX, player, player.aBoolean1541);
        }

    }

    private boolean interface_handle_dynamic_content_action(RSInterface class9) {
        int j = class9.content_type;
        if (network_friends_server_status == 2) {
            if (j == 201) {
                inputTaken = true;
                inputDialogState = 0;
                messagePromptRaised = true;
                promptInput = "";
                friendsListAction = 1;
                aString1121 = "Enter name of friend to add to list";
            }
            if (j == 202) {
                inputTaken = true;
                inputDialogState = 0;
                messagePromptRaised = true;
                promptInput = "";
                friendsListAction = 2;
                aString1121 = "Enter name of friend to delete from list";
            }
        }
        if (j == 205) {
            anInt1011 = 250;
            return true;
        }
        if (j == 501) {
            inputTaken = true;
            inputDialogState = 0;
            messagePromptRaised = true;
            promptInput = "";
            friendsListAction = 4;
            aString1121 = "Enter name of player to add to list";
        }
        if (j == 502) {
            inputTaken = true;
            inputDialogState = 0;
            messagePromptRaised = true;
            promptInput = "";
            friendsListAction = 5;
            aString1121 = "Enter name of player to delete from list";
        }
        if (j >= 300 && j <= 313) {
            int type = (j - 300) / 2;
            int direction = j & 1;
            int current_id = char_edit_idkits[type];
            if (current_id != -1) {
                do {
                    if (direction == 0 && --current_id < 0)
                        current_id = IdentityKit.length - 1;
                    if (direction == 1 && ++current_id >= IdentityKit.length)
                        current_id = 0;
                }
                while (IdentityKit.cache[current_id].notSelectable || IdentityKit.cache[current_id].bodyPartID != type + (char_edit_gender ? 0 : 7));
                char_edit_idkits[type] = current_id;
                char_edit_model_changed = true;
            }
        }
        if (j >= 314 && j <= 323) {
            int type = (j - 314) / 2;
            int direction = j & 1;
            int current_colour = char_edit_colours[type];
            if (direction == 0 && --current_colour < 0)
                current_colour = playerBodyRecolours[type].length - 1;
            if (direction == 1 && ++current_colour >= playerBodyRecolours[type].length)
                current_colour = 0;
            char_edit_colours[type] = current_colour;
            char_edit_model_changed = true;
        }
        if (j == 324 && !char_edit_gender) {
            char_edit_gender = true;
            char_edit_change_gender();
        }
        if (j == 325 && char_edit_gender) {
            char_edit_gender = false;
            char_edit_change_gender();
        }
        if (j == 326) {
            stream.p1isaac(101);
            stream.p1(char_edit_gender ? 0 : 1);
            for (int i1 = 0; i1 < 7; i1++)
                stream.p1(char_edit_idkits[i1]);

            for (int l1 = 0; l1 < 5; l1++)
                stream.p1(char_edit_colours[l1]);

            return true;
        }
        if (j == 613)
            report_abuse_mute_player = !report_abuse_mute_player;
        if (j >= 601 && j <= 612) {
            clearTopInterfaces();
            if (reportAbuseInput.length() > 0) {
                stream.p1isaac(218);
                stream.p8(TextClass.longForName(reportAbuseInput));
                stream.p1(j - 601);
                stream.p1(report_abuse_mute_player ? 1 : 0);
            }
        }
        return false;
    }

    private void refreshUpdateMasks(Packet stream) {
        for (int j = 0; j < sessionNpcsAwaitingUpdatePtr; j++) {
            int k = session_npcs_awaiting_update[j];
            Player player = session_players[k];
            int l = stream.g1();
            if ((l & 0x40) != 0)
                l += stream.g1() << 8;
            appendPlayerUpdateMask(l, k, stream, player);
        }

    }

    private void drawMapScenes(int y, int k, int x, int i1, int z) {
        int interactableObjectUID = sceneGraph.getWallObjectUID(z, x, y);
        if (interactableObjectUID != 0) {
            int l1 = sceneGraph.getIDTAGForXYZ(z, x, y, interactableObjectUID);
            int k2 = l1 >> 6 & 3;
            int i3 = l1 & 0x1f;
            int k3 = k;
            if (interactableObjectUID > 0)
                k3 = i1;
            int ai[] = minimapImage.myPixels;
            int k4 = 24624 + x * 4 + (103 - y) * 512 * 4;
            int i5 = interactableObjectUID >> 14 & 0x7fff;
            ObjectDef class46_2 = ObjectDef.forID(i5);
            if (class46_2.mapSceneID != -1) {
                IndexedImage indexedImage_2 = mapScenes[class46_2.mapSceneID];
                if (indexedImage_2 != null) {
                    int i6 = (class46_2.sizeX * 4 - indexedImage_2.imgWidth) / 2;
                    int j6 = (class46_2.sizeY * 4 - indexedImage_2.imgHeight) / 2;
                    indexedImage_2.drawImage(48 + x * 4 + i6, 48 + (104 - y - class46_2.sizeY) * 4 + j6);
                }
            } else {
                if (i3 == 0 || i3 == 2)
                    if (k2 == 0) {
                        ai[k4] = k3;
                        ai[k4 + 512] = k3;
                        ai[k4 + 1024] = k3;
                        ai[k4 + 1536] = k3;
                    } else if (k2 == 1) {
                        ai[k4] = k3;
                        ai[k4 + 1] = k3;
                        ai[k4 + 2] = k3;
                        ai[k4 + 3] = k3;
                    } else if (k2 == 2) {
                        ai[k4 + 3] = k3;
                        ai[k4 + 3 + 512] = k3;
                        ai[k4 + 3 + 1024] = k3;
                        ai[k4 + 3 + 1536] = k3;
                    } else if (k2 == 3) {
                        ai[k4 + 1536] = k3;
                        ai[k4 + 1536 + 1] = k3;
                        ai[k4 + 1536 + 2] = k3;
                        ai[k4 + 1536 + 3] = k3;
                    }
                if (i3 == 3)
                    if (k2 == 0)
                        ai[k4] = k3;
                    else if (k2 == 1)
                        ai[k4 + 3] = k3;
                    else if (k2 == 2)
                        ai[k4 + 3 + 1536] = k3;
                    else if (k2 == 3)
                        ai[k4 + 1536] = k3;
                if (i3 == 2)
                    if (k2 == 3) {
                        ai[k4] = k3;
                        ai[k4 + 512] = k3;
                        ai[k4 + 1024] = k3;
                        ai[k4 + 1536] = k3;
                    } else if (k2 == 0) {
                        ai[k4] = k3;
                        ai[k4 + 1] = k3;
                        ai[k4 + 2] = k3;
                        ai[k4 + 3] = k3;
                    } else if (k2 == 1) {
                        ai[k4 + 3] = k3;
                        ai[k4 + 3 + 512] = k3;
                        ai[k4 + 3 + 1024] = k3;
                        ai[k4 + 3 + 1536] = k3;
                    } else if (k2 == 2) {
                        ai[k4 + 1536] = k3;
                        ai[k4 + 1536 + 1] = k3;
                        ai[k4 + 1536 + 2] = k3;
                        ai[k4 + 1536 + 3] = k3;
                    }
            }
        }
        interactableObjectUID = sceneGraph.getInteractableObjectUID(z, x, y);
        if (interactableObjectUID != 0) {
            int i2 = sceneGraph.getIDTAGForXYZ(z, x, y, interactableObjectUID);
            int l2 = i2 >> 6 & 3;
            int j3 = i2 & 0x1f;
            int l3 = interactableObjectUID >> 14 & 0x7fff;
            ObjectDef class46_1 = ObjectDef.forID(l3);
            if (class46_1.mapSceneID != -1) {
                IndexedImage indexedImage_1 = mapScenes[class46_1.mapSceneID];
                if (indexedImage_1 != null) {
                    int j5 = (class46_1.sizeX * 4 - indexedImage_1.imgWidth) / 2;
                    int k5 = (class46_1.sizeY * 4 - indexedImage_1.imgHeight) / 2;
                    indexedImage_1.drawImage(48 + x * 4 + j5, 48 + (104 - y - class46_1.sizeY) * 4 + k5);
                }
            } else if (j3 == 9) {
                int l4 = 0xeeeeee;
                if (interactableObjectUID > 0)
                    l4 = 0xee0000;
                int ai1[] = minimapImage.myPixels;
                int l5 = 24624 + x * 4 + (103 - y) * 512 * 4;
                if (l2 == 0 || l2 == 2) {
                    ai1[l5 + 1536] = l4;
                    ai1[l5 + 1024 + 1] = l4;
                    ai1[l5 + 512 + 2] = l4;
                    ai1[l5 + 3] = l4;
                } else {
                    ai1[l5] = l4;
                    ai1[l5 + 512 + 1] = l4;
                    ai1[l5 + 1024 + 2] = l4;
                    ai1[l5 + 1536 + 3] = l4;
                }
            }
        }
        interactableObjectUID = sceneGraph.getGroundDecorationUID(z, x, y);
        if (interactableObjectUID != 0) {
            int j2 = interactableObjectUID >> 14 & 0x7fff;
            ObjectDef class46 = ObjectDef.forID(j2);
            if (class46.mapSceneID != -1) {
                IndexedImage indexedImage = mapScenes[class46.mapSceneID];
                if (indexedImage != null) {
                    int i4 = (class46.sizeX * 4 - indexedImage.imgWidth) / 2;
                    int j4 = (class46.sizeY * 4 - indexedImage.imgHeight) / 2;
                    indexedImage.drawImage(48 + x * 4 + i4, 48 + (104 - y - class46.sizeY) * 4 + j4);
                }
            }
        }
    }

    private void loadTitleScreen() {
        titlebox = new RgbImage("cache/sprites/title.png");
        passwordhover = new RgbImage("cache/sprites/pH.png");
        loginhover = new RgbImage("cache/sprites/LH.png");
        loadingBox1 = new RgbImage("cache/sprites/LoadingCircle/Loading1.png");
        aIndexedImageArray1152s = new IndexedImage[12];
        int j = 0;
        try {
            j = Integer.parseInt(getParameter("fl_icon"));
        } catch (Exception _ex) {
        }
        if (j == 0) {
            for (int k = 0; k < 12; k++)
                aIndexedImageArray1152s[k] = new IndexedImage(titleJagexArchive, "runes", k);

        } else {
            for (int l = 0; l < 12; l++)
                aIndexedImageArray1152s[l] = new IndexedImage(titleJagexArchive, "runes", 12 + (l & 3));

        }
        aClass30_Sub2_Sub1_Sub1_1201 = new RgbImage(128, 265);
        aClass30_Sub2_Sub1_Sub1_1202 = new RgbImage(128, 265);
        System.arraycopy(aRSImageProducer_1110.componentPixels, 0, aClass30_Sub2_Sub1_Sub1_1201.myPixels, 0, 33920);

        System.arraycopy(aRSImageProducer_1111.componentPixels, 0, aClass30_Sub2_Sub1_Sub1_1202.myPixels, 0, 33920);

        anIntArray851 = new int[256];
        for (int k1 = 0; k1 < 64; k1++)
            anIntArray851[k1] = k1 * 0x40000;

        for (int l1 = 0; l1 < 64; l1++)
            anIntArray851[l1 + 64] = 0xff0000 + 1024 * l1;

        for (int i2 = 0; i2 < 64; i2++)
            anIntArray851[i2 + 128] = 0xffff00 + 4 * i2;

        for (int j2 = 0; j2 < 64; j2++)
            anIntArray851[j2 + 192] = 0xffffff;

        anIntArray852 = new int[256];
        for (int k2 = 0; k2 < 64; k2++)
            anIntArray852[k2] = k2 * 1024;

        for (int l2 = 0; l2 < 64; l2++)
            anIntArray852[l2 + 64] = 65280 + 4 * l2;

        for (int i3 = 0; i3 < 64; i3++)
            anIntArray852[i3 + 128] = 65535 + 0x40000 * i3;

        for (int j3 = 0; j3 < 64; j3++)
            anIntArray852[j3 + 192] = 0xffffff;

        anIntArray853 = new int[256];
        for (int k3 = 0; k3 < 64; k3++)
            anIntArray853[k3] = k3 * 4;

        for (int l3 = 0; l3 < 64; l3++)
            anIntArray853[l3 + 64] = 255 + 0x40000 * l3;

        for (int i4 = 0; i4 < 64; i4++)
            anIntArray853[i4 + 128] = 0xff00ff + 1024 * i4;

        for (int j4 = 0; j4 < 64; j4++)
            anIntArray853[j4 + 192] = 0xffffff;

        anIntArray850 = new int[256];
        anIntArray1190 = new int[32768];
        anIntArray1191 = new int[32768];
        randomizeBackground(null);
        anIntArray828 = new int[32768];
        anIntArray829 = new int[32768];
        drawLoadingText(10, "Connecting to fileserver");
        if (!aBoolean831) {
            drawFlames = true;
            aBoolean831 = true;
            startRunnable(this, 2);
        }
    }

    private static void setHighMem() {
        SceneGraph.lowMem = false;
        Rasterizer.lowMem = false;
        lowMem = false;
        MapRegion.lowMem = false;
        ObjectDef.lowMem = false;
    }

    public static void main(String args[]) {
        try {
            if (args[0] != null)
                guiLaunch = false;
        } catch (Exception e) {
        }

        if (guiLaunch)
            try {
                network_worldid = 9;
                portOff = 0;
                setHighMem();
                isMembers = true;
                Signlink.storeid = 32;
                Signlink.startpriv(InetAddress.getLocalHost());
                @SuppressWarnings("unused") Client var_client = new Client();
                new SwingUI(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        else
            try {
                System.out.println("RS2 user rs2.Client - release #" + 317);
                if (args.length != 5) {
                    System.out.println("Usage: nodeid, port-offset, [lowmem/highmem], [free/members], storeid");
                    return;
                }
                network_worldid = Integer.parseInt(args[0]);
                portOff = Integer.parseInt(args[1]);
                if (args[2].equals("lowmem"))
                    setLowMem();
                else if (args[2].equals("highmem")) {
                    setHighMem();
                } else {
                    System.out.println("Usage: nodeid, port-offset, [lowmem/highmem], [free/members], storeid");
                    return;
                }
                if (args[3].equals("free"))
                    isMembers = false;
                else if (args[3].equals("members")) {
                    isMembers = true;
                } else {
                    System.out.println("Usage: nodeid, port-offset, [lowmem/highmem], [free/members], storeid");
                    return;
                }
                Signlink.storeid = Integer.parseInt(args[4]);
                Signlink.startpriv(InetAddress.getLocalHost());
                Client client1 = new Client();
                client1.initializeApp(765, 503);
            } catch (Exception exception) {
            }


    }

    private void loadingStages() {
        if (lowMem && loadingStage == 2 && MapRegion.anInt131 != plane) {
            gameScreenCanvas.initDrawingArea();
            plainFont.drawText(0, "Loading - please wait.", 151, 257);
            plainFont.drawText(0xffffff, "Loading - please wait.", 150, 256);
            gameScreenCanvas.drawGraphics(4, super.graphics, 4);
            loadingStage = 1;
            aLong824 = System.currentTimeMillis();
        }
        if (loadingStage == 1) {
            int j = method54();
            if (j != 0 && System.currentTimeMillis() - aLong824 > 0x57e40L) {
                Signlink.reporterror(myUsername + " glcfb " + aLong1215 + "," + j + "," + lowMem + "," + jagexFileStores[0] + "," + onDemandFetcher.getNodeCount() + "," + plane + "," + anInt1069 + "," + anInt1070);
                aLong824 = System.currentTimeMillis();
            }
        }
        if (loadingStage == 2 && plane != anInt985) {
            anInt985 = plane;
            rendedMapScene(plane);
        }
    }

    private int method54() {
        for (int i = 0; i < terrainData.length; i++) {
            if (terrainData[i] == null && terrainIndices[i] != -1)
                return -1;
            if (aByteArrayArray1247[i] == null && anIntArray1236[i] != -1)
                return -2;
        }

        boolean flag = true;
        for (int j = 0; j < terrainData.length; j++) {
            byte abyte0[] = aByteArrayArray1247[j];
            if (abyte0 != null) {
                int k = (mapCoordinates[j] >> 8) * 64 - baseX;
                int l = (mapCoordinates[j] & 0xff) * 64 - baseY;
                if (aBoolean1159) {
                    k = 10;
                    l = 10;
                }
                flag &= MapRegion.method189(k, abyte0, l);
            }
        }

        if (!flag)
            return -3;
        if (aBoolean1080) {
            return -4;
        } else {
            loadingStage = 2;
            MapRegion.anInt131 = plane;
            loadRegion();
            stream.p1isaac(121);
            return 0;
        }
    }

    private void renderProjectiles()//todo- confirm
    {
        for (Projectile class30_sub2_sub4_sub4 = (Projectile) aClass19_1013.getFront(); class30_sub2_sub4_sub4 != null; class30_sub2_sub4_sub4 = (Projectile) aClass19_1013.getNext())
            if (class30_sub2_sub4_sub4.anInt1597 != plane || currentTime > class30_sub2_sub4_sub4.anInt1572)
                class30_sub2_sub4_sub4.unlink();
            else if (currentTime >= class30_sub2_sub4_sub4.anInt1571) {
                if (class30_sub2_sub4_sub4.anInt1590 > 0) {
                    Npc npc = sessionNpcs[class30_sub2_sub4_sub4.anInt1590 - 1];
                    if (npc != null && npc.boundExtentX >= 0 && npc.boundExtentX < 13312 && npc.boundExtentY >= 0 && npc.boundExtentY < 13312)
                        class30_sub2_sub4_sub4.method455(currentTime, npc.boundExtentY, method42(class30_sub2_sub4_sub4.anInt1597, npc.boundExtentY, npc.boundExtentX) - class30_sub2_sub4_sub4.anInt1583, npc.boundExtentX);
                }
                if (class30_sub2_sub4_sub4.anInt1590 < 0) {
                    int j = -class30_sub2_sub4_sub4.anInt1590 - 1;
                    Player player;
                    if (j == unknownInt10)
                        player = session_player;
                    else
                        player = session_players[j];
                    if (player != null && player.boundExtentX >= 0 && player.boundExtentX < 13312 && player.boundExtentY >= 0 && player.boundExtentY < 13312)
                        class30_sub2_sub4_sub4.method455(currentTime, player.boundExtentY, method42(class30_sub2_sub4_sub4.anInt1597, player.boundExtentY, player.boundExtentX) - class30_sub2_sub4_sub4.anInt1583, player.boundExtentX);
                }
                class30_sub2_sub4_sub4.method456(anInt945);
                sceneGraph.addEntityA(plane, class30_sub2_sub4_sub4.anInt1595, (int) class30_sub2_sub4_sub4.aDouble1587, -1, (int) class30_sub2_sub4_sub4.aDouble1586, 60, (int) class30_sub2_sub4_sub4.aDouble1585, class30_sub2_sub4_sub4, false);
            }

    }

    public AppletContext getAppletContext() {
        if (Signlink.mainapp != null)
            return Signlink.mainapp.getAppletContext();
        else
            return super.getAppletContext();
    }

    private void drawLogo()//this isnt a logo its more of a login screen
    {

        byte abyte0[] = titleJagexArchive.getDataForName("title.dat");
        RgbImage rgbImage = new RgbImage(abyte0, this);
        aRSImageProducer_1110.initDrawingArea();
        rgbImage.method346(0, 0);
        aRSImageProducer_1111.initDrawingArea();
        rgbImage.method346(-637, 0);
        aRSImageProducer_1107.initDrawingArea();
        rgbImage.method346(-128, 0);
        aRSImageProducer_1108.initDrawingArea();
        rgbImage.method346(-202, -371);
        aRSImageProducer_1109.initDrawingArea();
        rgbImage.method346(-202, -171);
        aRSImageProducer_1112.initDrawingArea();
        rgbImage.method346(0, -265);
        aRSImageProducer_1113.initDrawingArea();
        rgbImage.method346(-562, -265);
        aRSImageProducer_1114.initDrawingArea();
        rgbImage.method346(-128, -171);
        aRSImageProducer_1115.initDrawingArea();
        rgbImage.method346(-562, -171);
        int ai[] = new int[rgbImage.myWidth];
        for (int j = 0; j < rgbImage.myHeight; j++) {
            for (int k = 0; k < rgbImage.myWidth; k++)
                ai[k] = rgbImage.myPixels[(rgbImage.myWidth - k - 1) + rgbImage.myWidth * j];

            System.arraycopy(ai, 0, rgbImage.myPixels, rgbImage.myWidth * j, rgbImage.myWidth);

        }

        aRSImageProducer_1110.initDrawingArea();
        rgbImage.method346(382, 0);
        aRSImageProducer_1111.initDrawingArea();
        rgbImage.method346(-255, 0);
        aRSImageProducer_1107.initDrawingArea();
        rgbImage.method346(254, 0);
        aRSImageProducer_1108.initDrawingArea();
        rgbImage.method346(180, -371);
        aRSImageProducer_1109.initDrawingArea();
        rgbImage.method346(180, -171);
        aRSImageProducer_1112.initDrawingArea();
        rgbImage.method346(382, -265);
        aRSImageProducer_1113.initDrawingArea();
        rgbImage.method346(-180, -265);
        aRSImageProducer_1114.initDrawingArea();
        rgbImage.method346(254, -171);
        aRSImageProducer_1115.initDrawingArea();
        rgbImage.method346(-180, -171);

        rgbImage = new RgbImage(titleJagexArchive, "logo", 0);
        aRSImageProducer_1107.initDrawingArea();
        //rgbImage.drawSprite(382 - rgbImage.myWidth / 2 - 128, 18);
        rgbImage = null;
        //Object obj = null;//never used
        //Object obj1 = null;//never used
        System.gc();


    }

    private void on_demand_process() {
        do {
            OnDemandData onDemandData;
            do {
                onDemandData = onDemandFetcher.getNextNode();
                if (onDemandData == null)
                    return;
                if (onDemandData.dataType == 0) {
                    Model.method460(onDemandData.buffer, onDemandData.ID);
                    if ((onDemandFetcher.getModelIndex(onDemandData.ID) & 0x62) != 0) {
                        needDrawTabArea = true;
                        if (backDialogID != -1)
                            inputTaken = true;
                    }
                }
                if (onDemandData.dataType == 1 && onDemandData.buffer != null)
                    rs2.Animation.load(onDemandData.buffer);
                if (onDemandData.dataType == 2 && onDemandData.ID == nextSong && onDemandData.buffer != null)
                    saveMidi(songChanging, onDemandData.buffer);
                if (onDemandData.dataType == 3 && loadingStage == 1) {
                    for (int i = 0; i < terrainData.length; i++) {
                        if (terrainIndices[i] == onDemandData.ID) {
                            terrainData[i] = onDemandData.buffer;
                            if (onDemandData.buffer == null)
                                terrainIndices[i] = -1;
                            break;
                        }
                        if (anIntArray1236[i] != onDemandData.ID)
                            continue;
                        aByteArrayArray1247[i] = onDemandData.buffer;
                        if (onDemandData.buffer == null)
                            anIntArray1236[i] = -1;
                        break;
                    }

                }
            } while (onDemandData.dataType != 93 || !onDemandFetcher.method564(onDemandData.ID));
            MapRegion.prefetchObjects(new Packet(onDemandData.buffer), onDemandFetcher);
        } while (true);
    }

    @SuppressWarnings("unused")
    private void calcFlamesPosition() {
        char c = '\u0100';
        for (int j = 10; j < 117; j++) {
            int k = (int) (Math.random() * 100D);
            if (k < 50)
                anIntArray828[j + (c - 2 << 7)] = 255;
        }
        for (int l = 0; l < 100; l++) {
            int i1 = (int) (Math.random() * 124D) + 2;
            int k1 = (int) (Math.random() * 128D) + 128;
            int k2 = i1 + (k1 << 7);
            anIntArray828[k2] = 192;
        }

        for (int j1 = 1; j1 < c - 1; j1++) {
            for (int l1 = 1; l1 < 127; l1++) {
                int l2 = l1 + (j1 << 7);
                anIntArray829[l2] = (anIntArray828[l2 - 1] + anIntArray828[l2 + 1] + anIntArray828[l2 - 128] + anIntArray828[l2 + 128]) / 4;
            }

        }

        anInt1275 += 128;
        if (anInt1275 > anIntArray1190.length) {
            anInt1275 -= anIntArray1190.length;
            int i2 = (int) (Math.random() * 12D);
            randomizeBackground(aIndexedImageArray1152s[i2]);
        }
        for (int j2 = 1; j2 < c - 1; j2++) {
            for (int i3 = 1; i3 < 127; i3++) {
                int k3 = i3 + (j2 << 7);
                int i4 = anIntArray829[k3 + 128] - anIntArray1190[k3 + anInt1275 & anIntArray1190.length - 1] / 5;
                if (i4 < 0)
                    i4 = 0;
                anIntArray828[k3] = i4;
            }

        }

        System.arraycopy(anIntArray969, 1, anIntArray969, 0, c - 1);

        anIntArray969[c - 1] = (int) (Math.sin((double) currentTime / 14D) * 16D + Math.sin((double) currentTime / 15D) * 14D + Math.sin((double) currentTime / 16D) * 12D);
        if (anInt1040 > 0)
            anInt1040 -= 4;
        if (anInt1041 > 0)
            anInt1041 -= 4;
        if (anInt1040 == 0 && anInt1041 == 0) {
            int l3 = (int) (Math.random() * 2000D);
            if (l3 == 0)
                anInt1040 = 1024;
            if (l3 == 1)
                anInt1041 = 1024;
        }
    }

    private boolean saveWave(byte abyte0[], int i) {
        return abyte0 == null || Signlink.wavesave(abyte0, i);
    }

    private void method60(int i) {
        RSInterface class9 = RSInterface.interfaceCache[i];
        for (int j = 0; j < class9.children.length; j++) {
            if (class9.children[j] == -1)
                break;
            RSInterface class9_1 = RSInterface.interfaceCache[class9.children[j]];
            if (class9_1.type == 1)
                method60(class9_1.id);
            class9_1.anim_frame = 0;
            class9_1.anInt208 = 0;
        }
    }

    private void drawHeadIcon() {
        if (anInt855 != 2)
            return;
        calcEntityScreenPos((anInt934 - baseX << 7) + anInt937, anInt936 * 2, (anInt935 - baseY << 7) + anInt938);
        if (spriteDrawX > -1 && currentTime % 20 < 10)
            headIcons[2].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
    }

    private void client_process() {
        if (anInt1104 > 1)
            anInt1104--;
        if (anInt1011 > 0)
            anInt1011--;
        for (int j = 0; j < 5; j++)
            if (!parsePacket())
                break;

        if (!loggedIn)
            return;
        synchronized (mouseDetection.syncObject) {
            if (flagged) {
                if (super.mouseButtonPressed != 0 || mouseDetection.coordsIndex >= 40) {
                    stream.p1isaac(45);
                    stream.p1(0);
                    int j2 = stream.pos;
                    int j3 = 0;
                    for (int j4 = 0; j4 < mouseDetection.coordsIndex; j4++) {
                        if (j2 - stream.pos >= 240)
                            break;
                        j3++;
                        int l4 = mouseDetection.coordsY[j4];
                        if (l4 < 0)
                            l4 = 0;
                        else if (l4 > 502)
                            l4 = 502;
                        int k5 = mouseDetection.coordsX[j4];
                        if (k5 < 0)
                            k5 = 0;
                        else if (k5 > 764)
                            k5 = 764;
                        int i6 = l4 * 765 + k5;
                        if (mouseDetection.coordsY[j4] == -1 && mouseDetection.coordsX[j4] == -1) {
                            k5 = -1;
                            l4 = -1;
                            i6 = 0x7ffff;
                        }
                        if (k5 == anInt1237 && l4 == anInt1238) {
                            if (anInt1022 < 2047)
                                anInt1022++;
                        } else {
                            int j6 = k5 - anInt1237;
                            anInt1237 = k5;
                            int k6 = l4 - anInt1238;
                            anInt1238 = l4;
                            if (anInt1022 < 8 && j6 >= -32 && j6 <= 31 && k6 >= -32 && k6 <= 31) {
                                j6 += 32;
                                k6 += 32;
                                stream.p2((anInt1022 << 12) + (j6 << 6) + k6);
                                anInt1022 = 0;
                            } else if (anInt1022 < 8) {
                                stream.p3(0x800000 + (anInt1022 << 19) + i6);
                                anInt1022 = 0;
                            } else {
                                stream.p4(0xc0000000 + (anInt1022 << 19) + i6);
                                anInt1022 = 0;
                            }
                        }
                    }

                    stream.psize1(stream.pos - j2);
                    if (j3 >= mouseDetection.coordsIndex) {
                        mouseDetection.coordsIndex = 0;
                    } else {
                        mouseDetection.coordsIndex -= j3;
                        for (int i5 = 0; i5 < mouseDetection.coordsIndex; i5++) {
                            mouseDetection.coordsX[i5] = mouseDetection.coordsX[i5 + j3];
                            mouseDetection.coordsY[i5] = mouseDetection.coordsY[i5 + j3];
                        }

                    }
                }
            } else {
                mouseDetection.coordsIndex = 0;
            }
        }
        if (super.mouseButtonPressed != 0) {
            long l = (super.clickTime - aLong1220) / 50L;
            if (l > 4095L)
                l = 4095L;
            aLong1220 = super.clickTime;
            int k2 = super.clickY;
            if (k2 < 0)
                k2 = 0;
            else if (k2 > 502)
                k2 = 502;
            int k3 = super.clickX;
            if (k3 < 0)
                k3 = 0;
            else if (k3 > 764)
                k3 = 764;
            int k4 = k2 * 765 + k3;
            int j5 = 0;
            if (super.mouseButtonPressed == 2)
                j5 = 1;
            int l5 = (int) l;
            stream.p1isaac(241);
            stream.p4((l5 << 20) + (j5 << 19) + k4);
        }
        if (anInt1016 > 0)
            anInt1016--;
        if (super.keyStatus[1] == 1 || super.keyStatus[2] == 1 || super.keyStatus[3] == 1 || super.keyStatus[4] == 1)
            aBoolean1017 = true;
        if (aBoolean1017 && anInt1016 <= 0) {
            anInt1016 = 20;
            aBoolean1017 = false;
            stream.p1isaac(86);
            stream.p2(anInt1184);
            stream.sp2(minimapInt1);
        }
        if (super.awtFocus && !aBoolean954) {
            aBoolean954 = true;
            stream.p1isaac(3);
            stream.p1(1);
        }
        if (!super.awtFocus && aBoolean954) {
            aBoolean954 = false;
            stream.p1isaac(3);
            stream.p1(0);
        }
        loadingStages();
        method115();
        method90();
        anInt1009++;
        if (anInt1009 > 750)
            dropClient();
        updatePlayerInstances();
        forceNpcUpdateBlock();
        resetSpokenText();
        anInt945++;
        if (crossType != 0) {
            crossIndex += 20;
            if (crossIndex >= 400)
                crossType = 0;
        }
        if (atInventoryInterfaceType != 0) {
            atInventoryLoopCycle++;
            if (atInventoryLoopCycle >= 15) {
                if (atInventoryInterfaceType == 2)
                    needDrawTabArea = true;
                if (atInventoryInterfaceType == 3)
                    inputTaken = true;
                atInventoryInterfaceType = 0;
            }
        }
        if (activeInterfaceType != 0) {
            anInt989++;
            if (super.mouseEventX > anInt1087 + 5 || super.mouseEventX < anInt1087 - 5 || super.mouseEventY > anInt1088 + 5 || super.mouseEventY < anInt1088 - 5)
                aBoolean1242 = true;
            if (super.mouseButtonDown == 0) {
                if (activeInterfaceType == 2)
                    needDrawTabArea = true;
                if (activeInterfaceType == 3)
                    inputTaken = true;
                activeInterfaceType = 0;
                if (aBoolean1242 && anInt989 >= 5) {
                    lastActiveInvInterface = -1;
                    processRightClick();
                    if (lastActiveInvInterface == anInt1084 && mouseInvInterfaceIndex != anInt1085) {
                        RSInterface class9 = RSInterface.interfaceCache[anInt1084];
                        int j1 = 0;
                        if (anInt913 == 1 && class9.content_type == 206)
                            j1 = 1;
                        if (class9.inv[mouseInvInterfaceIndex] <= 0)
                            j1 = 0;
                        if (class9.aBoolean235) {
                            int l2 = anInt1085;
                            int l3 = mouseInvInterfaceIndex;
                            class9.inv[l3] = class9.inv[l2];
                            class9.invStackSizes[l3] = class9.invStackSizes[l2];
                            class9.inv[l2] = -1;
                            class9.invStackSizes[l2] = 0;
                        } else if (j1 == 1) {
                            int i3 = anInt1085;
                            for (int i4 = mouseInvInterfaceIndex; i3 != i4; )
                                if (i3 > i4) {
                                    class9.swapInventoryItems(i3, i3 - 1);
                                    i3--;
                                } else if (i3 < i4) {
                                    class9.swapInventoryItems(i3, i3 + 1);
                                    i3++;
                                }

                        } else {
                            class9.swapInventoryItems(anInt1085, mouseInvInterfaceIndex);
                        }
                        stream.p1isaac(214);
                        stream.isp2(anInt1084);
                        stream.np1(j1);
                        stream.isp2(anInt1085);
                        stream.ip2(mouseInvInterfaceIndex);
                    }
                } else if ((anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2)
                    determineMenuSize();
                else if (menuActionRow > 0)
                    doAction(menuActionRow - 1);
                atInventoryLoopCycle = 10;
                super.mouseButtonPressed = 0;
            }
        }
        if (SceneGraph.clickedTileX != -1) {
            int k = SceneGraph.clickedTileX;
            int k1 = SceneGraph.clickedTileY;
            boolean flag = doWalkTo(0, 0, 0, 0, session_player.pathY[0], 0, 0, k1, session_player.pathX[0], true, k);
            SceneGraph.clickedTileX = -1;
            if (flag) {
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 1;
                crossIndex = 0;
            }
        }
        if (super.mouseButtonPressed == 1 && aString844 != null) {
            aString844 = null;
            inputTaken = true;
            super.mouseButtonPressed = 0;
        }
        processMenuClick();
        processMainScreenClick();
        processTabClick();
        processChatModeClick();
        if (super.mouseButtonDown == 1 || super.mouseButtonPressed == 1)
            anInt1213++;
        if (loadingStage == 2)
            method108();
        if (loadingStage == 2 && aBoolean1160)
            calcCameraPos();
        for (int i1 = 0; i1 < 5; i1++)
            anIntArray1030[i1]++;

        manageTextInputs();
        super.idleTime++;
        if (super.idleTime > 4500) {
            anInt1011 = 250;
            super.idleTime -= 500;
            stream.p1isaac(202);
        }
        anInt988++;
        if (anInt988 > 500) {
            anInt988 = 0;
            int l1 = (int) (Math.random() * 8D);
            if ((l1 & 1) == 1)
                anInt1278 += anInt1279;
            if ((l1 & 2) == 2)
                anInt1131 += anInt1132;
            if ((l1 & 4) == 4)
                anInt896 += anInt897;
        }
        if (anInt1278 < -50)
            anInt1279 = 2;
        if (anInt1278 > 50)
            anInt1279 = -2;
        if (anInt1131 < -55)
            anInt1132 = 2;
        if (anInt1131 > 55)
            anInt1132 = -2;
        if (anInt896 < -40)
            anInt897 = 1;
        if (anInt896 > 40)
            anInt897 = -1;
        anInt1254++;
        if (anInt1254 > 500) {
            anInt1254 = 0;
            int i2 = (int) (Math.random() * 8D);
            if ((i2 & 1) == 1)
                minimapInt2 += anInt1210;
            if ((i2 & 2) == 2)
                minimapInt3 += anInt1171;
        }
        if (minimapInt2 < -60)
            anInt1210 = 2;
        if (minimapInt2 > 60)
            anInt1210 = -2;
        if (minimapInt3 < -20)
            anInt1171 = 1;
        if (minimapInt3 > 10)
            anInt1171 = -1;
        anInt1010++;
        if (anInt1010 > 50)
            stream.p1isaac(0);
        try {
            if (socketStream != null && stream.pos > 0) {
                socketStream.queueBytes(stream.pos, stream.data);
                stream.pos = 0;
                anInt1010 = 0;
            }
        } catch (IOException _ex) {
            dropClient();
        } catch (Exception exception) {
            network_disconnect();
        }
    }

    private void method63() {
        GameObjectSpawnRequest gameObjectSpawnRequest = (GameObjectSpawnRequest) aClass19_1179.getFront();
        for (; gameObjectSpawnRequest != null; gameObjectSpawnRequest = (GameObjectSpawnRequest) aClass19_1179.getNext())
            if (gameObjectSpawnRequest.anInt1294 == -1) {
                gameObjectSpawnRequest.anInt1302 = 0;
                method89(gameObjectSpawnRequest);
            } else {
                gameObjectSpawnRequest.unlink();
            }

    }

    private void resetImageProducers() {
        if (aRSImageProducer_1107 != null)
            return;
        super.fullGameScreen = null;
        aRSImageProducer_1166 = null;
        minimapIP = null;
        tabAreaDrawingTarget = null;
        gameScreenCanvas = null;
        aRSImageProducer_1123 = null;
        aRSImageProducer_1124 = null;
        aRSImageProducer_1125 = null;
        aRSImageProducer_1110 = new GraphicsBuffer(128, 265, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1111 = new GraphicsBuffer(128, 265, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1107 = new GraphicsBuffer(509, 171, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1108 = new GraphicsBuffer(360, 255, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1109 = new GraphicsBuffer(360, 264, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1112 = new GraphicsBuffer(202, 238, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1113 = new GraphicsBuffer(203, 238, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1114 = new GraphicsBuffer(74, 94, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1115 = new GraphicsBuffer(75, 94, getGameComponent());
        Graphics2D.resetImage();
        if (titleJagexArchive != null) {
            drawLogo();
            loadTitleScreen();
        }
        repaintRequested = true;
    }

    protected void drawLoadingText(int i, String s) {
        anInt1079 = i;
        aString1049 = s;
        resetImageProducers();
        if (titleJagexArchive == null) {
            super.drawLoadingText(i, s);
            return;
        }
        aRSImageProducer_1109.initDrawingArea();//loading area ??
        char c = '\u0168';//what are these?
        char c1 = '\310';
        byte byte1 = 20;
        boldFont.drawText(0xffffff, "RuneScape is loading - please wait...", c1 / 2 - 26 - byte1, c / 2);
        int j = c1 / 2 - 18 - byte1;
        Graphics2D.drawRect(c / 2 - 152, j, 304, 34, 0x8c1111);
        Graphics2D.drawRect(c / 2 - 151, j + 1, 302, 32, 0);
        Graphics2D.fillRect(c / 2 - 150, j + 2, i * 3, 30, 0x8c1111);
        Graphics2D.fillRect((c / 2 - 150) + i * 3, j + 2, 300 - i * 3, 30, 0);
        boldFont.drawText(0xffffff, s, (c1 / 2 + 5) - byte1, c / 2);
        aRSImageProducer_1109.drawGraphics(171, super.graphics, 202);
        if (repaintRequested) {
            repaintRequested = false;
            if (!aBoolean831) {
                aRSImageProducer_1110.drawGraphics(0, super.graphics, 0);
                aRSImageProducer_1111.drawGraphics(0, super.graphics, 637);
            }
            aRSImageProducer_1107.drawGraphics(0, super.graphics, 128);
            aRSImageProducer_1108.drawGraphics(371, super.graphics, 202);
            aRSImageProducer_1112.drawGraphics(265, super.graphics, 0);
            aRSImageProducer_1113.drawGraphics(265, super.graphics, 562);
            aRSImageProducer_1114.drawGraphics(171, super.graphics, 128);
            aRSImageProducer_1115.drawGraphics(171, super.graphics, 562);
        }
    }

    private void method65(int i, int j, int k, int l, RSInterface class9, int i1, boolean flag, int j1) {
        int anInt992;
        if (aBoolean972)
            anInt992 = 32;
        else
            anInt992 = 0;
        aBoolean972 = false;
        if (k >= i && k < i + 16 && l >= i1 && l < i1 + 16) {
            class9.scrollPosition -= anInt1213 * 4;
            if (flag) {
                needDrawTabArea = true;
            }
        } else if (k >= i && k < i + 16 && l >= (i1 + j) - 16 && l < i1 + j) {
            class9.scrollPosition += anInt1213 * 4;
            if (flag) {
                needDrawTabArea = true;
            }
        } else if (k >= i - anInt992 && k < i + 16 + anInt992 && l >= i1 + 16 && l < (i1 + j) - 16 && anInt1213 > 0) {
            int l1 = ((j - 32) * j) / j1;
            if (l1 < 8)
                l1 = 8;
            int i2 = l - i1 - 16 - l1 / 2;
            int j2 = j - 32 - l1;
            class9.scrollPosition = ((j1 - j) * i2) / j2;
            if (flag)
                needDrawTabArea = true;
            aBoolean972 = true;
        }
    }

    private boolean clickObject(int i, int j, int k)//todo- confirm
    {
        int i1 = i >> 14 & 0x7fff;
        int j1 = sceneGraph.getIDTAGForXYZ(plane, k, j, i);
        if (j1 == -1)
            return false;
        int k1 = j1 & 0x1f;
        int l1 = j1 >> 6 & 3;
        if (k1 == 10 || k1 == 11 || k1 == 22) {
            ObjectDef class46 = ObjectDef.forID(i1);
            int i2;
            int j2;
            if (l1 == 0 || l1 == 2) {
                i2 = class46.sizeX;
                j2 = class46.sizeY;
            } else {
                i2 = class46.sizeY;
                j2 = class46.sizeX;
            }
            int k2 = class46.anInt768;
            if (l1 != 0)
                k2 = (k2 << l1 & 0xf) + (k2 >> 4 - l1);
            doWalkTo(2, 0, j2, 0, session_player.pathY[0], i2, k2, j, session_player.pathX[0], false, k);
        } else {
            doWalkTo(2, l1, 0, k1 + 1, session_player.pathY[0], 0, 0, j, session_player.pathX[0], false, k);
        }
        crossX = super.clickX;
        crossY = super.clickY;
        crossType = 2;
        crossIndex = 0;
        return true;
    }

    private JagexArchive streamLoaderForName(int i, String s, String s1, int j, int k) {
        byte abyte0[] = null;
        int l = 5;
        try {
            if (jagexFileStores[0] != null)
                abyte0 = jagexFileStores[0].decompress(i);
        } catch (Exception _ex) {
        }
        if (abyte0 != null) {
            //        aCRC32_930.reset();
            //        aCRC32_930.update(abyte0);
            //        int i1 = (int)aCRC32_930.getValue();
            //        if(i1 != j)
        }
        if (abyte0 != null) {
            JagexArchive jagexArchive = new JagexArchive(abyte0);
            return jagexArchive;
        }
        int j1 = 0;
        while (abyte0 == null) {
            String s2 = "Unknown error";
            drawLoadingText(k, "Requesting " + s);
            //Object obj = null;//never used
            try {
                int k1 = 0;
                DataInputStream datainputstream = openJagGrabInputStream(s1 + j);
                byte abyte1[] = new byte[6];
                datainputstream.readFully(abyte1, 0, 6);
                Packet stream = new Packet(abyte1);
                stream.pos = 3;
                int i2 = stream.g3() + 6;
                int j2 = 6;
                abyte0 = new byte[i2];
                System.arraycopy(abyte1, 0, abyte0, 0, 6);

                while (j2 < i2) {
                    int l2 = i2 - j2;
                    if (l2 > 1000)
                        l2 = 1000;
                    int j3 = datainputstream.read(abyte0, j2, l2);
                    if (j3 < 0) {
                        s2 = "Length error: " + j2 + "/" + i2;
                        throw new IOException("EOF");
                    }
                    j2 += j3;
                    int k3 = (j2 * 100) / i2;
                    if (k3 != k1)
                        drawLoadingText(k, "Loading " + s + " - " + k3 + "%");
                    k1 = k3;
                }
                datainputstream.close();
                try {
                    if (jagexFileStores[0] != null)
                        jagexFileStores[0].put(abyte0.length, abyte0, i);
                } catch (Exception _ex) {
                    jagexFileStores[0] = null;
                }
                /*             if(abyte0 != null)
				{
					aCRC32_930.reset();
					aCRC32_930.update(abyte0);
					int i3 = (int)aCRC32_930.getValue();
					if(i3 != j)
					{
						abyte0 = null;
						j1++;
						s2 = "Checksum error: " + i3;
					}
				}
*/
            } catch (IOException ioexception) {
                if (s2.equals("Unknown error"))
                    s2 = "Connection error";
                abyte0 = null;
            } catch (NullPointerException _ex) {
                s2 = "Null error";
                abyte0 = null;
                if (!Signlink.reporterror)
                    return null;
            } catch (ArrayIndexOutOfBoundsException _ex) {
                s2 = "Bounds error";
                abyte0 = null;
                if (!Signlink.reporterror)
                    return null;
            } catch (Exception _ex) {
                s2 = "Unexpected error";
                abyte0 = null;
                if (!Signlink.reporterror)
                    return null;
            }
            if (abyte0 == null) {
                for (int l1 = l; l1 > 0; l1--) {
                    if (j1 >= 3) {
                        drawLoadingText(k, "Game updated - please reload page");
                        l1 = 10;
                    } else {
                        drawLoadingText(k, s2 + " - Retrying in " + l1);
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception _ex) {
                    }
                }

                l *= 2;
                if (l > 60)
                    l = 60;
                aBoolean872 = !aBoolean872;
            }

        }

        JagexArchive jagexArchive_1 = new JagexArchive(abyte0);
        return jagexArchive_1;
    }

    private void dropClient() {
        if (anInt1011 > 0) {
            network_disconnect();
            return;
        }
        gameScreenCanvas.initDrawingArea();
        plainFont.drawText(0, "Connection lost", 144, 257);
        plainFont.drawText(0xffffff, "Connection lost", 143, 256);
        plainFont.drawText(0, "Please wait - attempting to reestablish", 159, 257);
        plainFont.drawText(0xffffff, "Please wait - attempting to reestablish", 158, 256);
        gameScreenCanvas.drawGraphics(4, super.graphics, 4);
        miniMapLock = 0;
        destX = 0;
        RSSocket rsSocket = socketStream;
        loggedIn = false;
        loginFailures = 0;
        login(myUsername, myPassword, true);
        if (!loggedIn)
            network_disconnect();
        try {
            rsSocket.close();
        } catch (Exception _ex) {
        }
    }

    private void doAction(int i) {
        if (i < 0)
            return;
        if (inputDialogState != 0) {
            inputDialogState = 0;
            inputTaken = true;
        }
        int j = menuActionCmd2[i];
        int k = menuActionCmd3[i];
        int l = menuActionID[i];
        int i1 = menuActionCmd1[i];
        if (l >= 2000)
            l -= 2000;
        if (l == 582) {
            Npc npc = sessionNpcs[i1];
            if (npc != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, npc.pathY[0], session_player.pathX[0], false, npc.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(57);
                stream.sp2(anInt1285);
                stream.sp2(i1);
                stream.ip2(anInt1283);
                stream.sp2(anInt1284);
            }
        }
        if (l == 1005) {
            if (runIsOn == true) {
                runIsOn = false;
                stream.p1isaac(185);
                stream.p2(152);
            } else if (runIsOn == false) {
                runIsOn = true;
                stream.p1isaac(185);
                stream.p2(153);
            }
        }
        if (l == 234) {
            boolean flag1 = doWalkTo(2, 0, 0, 0, session_player.pathY[0], 0, 0, k, session_player.pathX[0], false, j);
            if (!flag1)
                flag1 = doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, k, session_player.pathX[0], false, j);
            crossX = super.clickX;
            crossY = super.clickY;
            crossType = 2;
            crossIndex = 0;
            stream.p1isaac(236);
            stream.ip2(k + baseY);
            stream.p2(i1);
            stream.ip2(j + baseX);
        }
        if (l == 62 && clickObject(i1, k, j)) {
            stream.p1isaac(192);
            stream.p2(anInt1284);
            stream.ip2(i1 >> 14 & 0x7fff);
            stream.isp2(k + baseY);
            stream.ip2(anInt1283);
            stream.isp2(j + baseX);
            stream.p2(anInt1285);
        }
        if (l == 511) {
            boolean flag2 = doWalkTo(2, 0, 0, 0, session_player.pathY[0], 0, 0, k, session_player.pathX[0], false, j);
            if (!flag2)
                flag2 = doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, k, session_player.pathX[0], false, j);
            crossX = super.clickX;
            crossY = super.clickY;
            crossType = 2;
            crossIndex = 0;
            stream.p1isaac(25);
            stream.ip2(anInt1284);
            stream.sp2(anInt1285);
            stream.p2(i1);
            stream.sp2(k + baseY);
            stream.isp2(anInt1283);
            stream.p2(j + baseX);
        }
        if (l == 74) {
            stream.p1isaac(122);
            stream.isp2(k);
            stream.sp2(j);
            stream.ip2(i1);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 315) {
            RSInterface class9 = RSInterface.interfaceCache[k];
            boolean flag8 = true;
            if (class9.content_type > 0)
                flag8 = interface_handle_dynamic_content_action(class9);
            if (flag8) {
                stream.p1isaac(185);
                stream.p2(k);
            }
        }
        if (l == 561) {
            Player player = session_players[i1];
            if (player != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, player.pathY[0], session_player.pathX[0], false, player.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                anInt1188 += i1;
                if (anInt1188 >= 90) {
                    stream.p1isaac(136);
                    anInt1188 = 0;
                }
                stream.p1isaac(128);
                stream.p2(i1);
            }
        }
        if (l == 20) {
            Npc class30_sub2_sub4_sub1_sub1_1 = sessionNpcs[i1];
            if (class30_sub2_sub4_sub1_sub1_1 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_1.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub1_1.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(155);
                stream.ip2(i1);
            }
        }
        if (l == 779) {
            Player class30_sub2_sub4_sub1_sub2_1 = session_players[i1];
            if (class30_sub2_sub4_sub1_sub2_1 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_1.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub2_1.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(153);
                stream.ip2(i1);
            }
        }
        if (l == 516)
            if (!menuOpen)
                sceneGraph.request2DTrace(super.clickY - 4, super.clickX - 4);
            else
                sceneGraph.request2DTrace(k - 4, j - 4);
        if (l == 1062) {
            anInt924 += baseX;
            if (anInt924 >= 113) {
                stream.p1isaac(183);
                stream.p3(0xe63271);
                anInt924 = 0;
            }
            clickObject(i1, k, j);
            stream.p1isaac(228);
            stream.sp2(i1 >> 14 & 0x7fff);
            stream.sp2(k + baseY);
            stream.p2(j + baseX);
        }
        if (l == 679 && !aBoolean1149) {
            stream.p1isaac(40);
            stream.p2(k);
            aBoolean1149 = true;
        }
        if (l == 431) {
            stream.p1isaac(129);
            stream.sp2(j);
            stream.p2(k);
            stream.sp2(i1);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 337 || l == 42 || l == 792 || l == 322) {
            String s = menuActionName[i];
            int k1 = s.indexOf("@whi@");
            if (k1 != -1) {
                long l3 = TextClass.longForName(s.substring(k1 + 5).trim());
                if (l == 337)
                    addFriend(l3);
                if (l == 42)
                    addIgnore(l3);
                if (l == 792)
                    delFriend(l3);
                if (l == 322)
                    delIgnore(l3);
            }
        }
        if (l == 53) {
            stream.p1isaac(135);
            stream.ip2(j);
            stream.sp2(k);
            stream.ip2(i1);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 539) {
            stream.p1isaac(16);
            stream.sp2(i1);
            stream.isp2(j);
            stream.isp2(k);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 484 || l == 6) {
            String s1 = menuActionName[i];
            int l1 = s1.indexOf("@whi@");
            if (l1 != -1) {
                s1 = s1.substring(l1 + 5).trim();
                String s7 = TextClass.fixName(TextClass.nameForLong(TextClass.longForName(s1)));
                boolean flag9 = false;
                for (int j3 = 0; j3 < session_player_count; j3++) {
                    Player class30_sub2_sub4_sub1_sub2_7 = session_players[session_player_list[j3]];
                    if (class30_sub2_sub4_sub1_sub2_7 == null || class30_sub2_sub4_sub1_sub2_7.name == null || !class30_sub2_sub4_sub1_sub2_7.name.equalsIgnoreCase(s7))
                        continue;
                    doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_7.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub2_7.pathX[0]);
                    if (l == 484) {
                        stream.p1isaac(139);
                        stream.ip2(session_player_list[j3]);
                    }
                    if (l == 6) {
                        anInt1188 += i1;
                        if (anInt1188 >= 90) {
                            stream.p1isaac(136);
                            anInt1188 = 0;
                        }
                        stream.p1isaac(128);
                        stream.p2(session_player_list[j3]);
                    }
                    flag9 = true;
                    break;
                }

                if (!flag9)
                    pushMessage("Unable to find " + s7, 0, "");
            }
        }
        if (l == 870) {
            stream.p1isaac(53);
            stream.p2(j);
            stream.sp2(anInt1283);
            stream.isp2(i1);
            stream.p2(anInt1284);
            stream.ip2(anInt1285);
            stream.p2(k);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 847) {
            stream.p1isaac(87);
            stream.sp2(i1);
            stream.p2(k);
            stream.sp2(j);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 626) {
            RSInterface class9_1 = RSInterface.interfaceCache[k];
            spellSelected = 1;
            anInt1137 = k;
            spellUsableOn = class9_1.spellUsableOn;
            itemSelected = 0;
            needDrawTabArea = true;
            String s4 = class9_1.selectedActionName;
            if (s4.indexOf(" ") != -1)
                s4 = s4.substring(0, s4.indexOf(" "));
            String s8 = class9_1.selectedActionName;
            if (s8.indexOf(" ") != -1)
                s8 = s8.substring(s8.indexOf(" ") + 1);
            spellTooltip = s4 + " " + class9_1.spellName + " " + s8;
            if (spellUsableOn == 16) {
                needDrawTabArea = true;
                tabID = 3;
                tabAreaAltered = true;
            }
            return;
        }
        if (l == 78) {
            stream.p1isaac(117);
            stream.isp2(k);
            stream.isp2(i1);
            stream.ip2(j);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 27) {
            Player class30_sub2_sub4_sub1_sub2_2 = session_players[i1];
            if (class30_sub2_sub4_sub1_sub2_2 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_2.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub2_2.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                anInt986 += i1;
                if (anInt986 >= 54) {
                    stream.p1isaac(189);
                    stream.p1(234);
                    anInt986 = 0;
                }
                stream.p1isaac(73);
                stream.ip2(i1);
            }
        }
        if (l == 213) {
            boolean flag3 = doWalkTo(2, 0, 0, 0, session_player.pathY[0], 0, 0, k, session_player.pathX[0], false, j);
            if (!flag3)
                flag3 = doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, k, session_player.pathX[0], false, j);
            crossX = super.clickX;
            crossY = super.clickY;
            crossType = 2;
            crossIndex = 0;
            stream.p1isaac(79);
            stream.ip2(k + baseY);
            stream.p2(i1);
            stream.sp2(j + baseX);
        }
        if (l == 632) {
            stream.p1isaac(145);
            stream.sp2(k);
            stream.sp2(j);
            stream.sp2(i1);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 493) {
            stream.p1isaac(75);
            stream.isp2(k);
            stream.ip2(j);
            stream.sp2(i1);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 652) {
            boolean flag4 = doWalkTo(2, 0, 0, 0, session_player.pathY[0], 0, 0, k, session_player.pathX[0], false, j);
            if (!flag4)
                flag4 = doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, k, session_player.pathX[0], false, j);
            crossX = super.clickX;
            crossY = super.clickY;
            crossType = 2;
            crossIndex = 0;
            stream.p1isaac(156);
            stream.sp2(j + baseX);
            stream.ip2(k + baseY);
            stream.isp2(i1);
        }
        if (l == 94) {
            boolean flag5 = doWalkTo(2, 0, 0, 0, session_player.pathY[0], 0, 0, k, session_player.pathX[0], false, j);
            if (!flag5)
                flag5 = doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, k, session_player.pathX[0], false, j);
            crossX = super.clickX;
            crossY = super.clickY;
            crossType = 2;
            crossIndex = 0;
            stream.p1isaac(181);
            stream.ip2(k + baseY);
            stream.p2(i1);
            stream.ip2(j + baseX);
            stream.sp2(anInt1137);
        }
        if (l == 646) {
            stream.p1isaac(185);
            stream.p2(k);
            RSInterface class9_2 = RSInterface.interfaceCache[k];
            if (class9_2.dynamic_value_formulas != null && class9_2.dynamic_value_formulas[0][0] == 5) {
                int i2 = class9_2.dynamic_value_formulas[0][1];
                if (sessionSettings[i2] != class9_2.condition_value_to_compare[0]) {
                    sessionSettings[i2] = class9_2.condition_value_to_compare[0];
                    applyConfigChange(i2);
                    needDrawTabArea = true;
                }
            }
        }
        if (l == 225) {
            Npc class30_sub2_sub4_sub1_sub1_2 = sessionNpcs[i1];
            if (class30_sub2_sub4_sub1_sub1_2 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_2.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub1_2.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                anInt1226 += i1;
                if (anInt1226 >= 85) {
                    stream.p1isaac(230);
                    stream.p1(239);
                    anInt1226 = 0;
                }
                stream.p1isaac(17);
                stream.isp2(i1);
            }
        }
        if (l == 965) {
            Npc class30_sub2_sub4_sub1_sub1_3 = sessionNpcs[i1];
            if (class30_sub2_sub4_sub1_sub1_3 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_3.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub1_3.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                anInt1134++;
                if (anInt1134 >= 96) {
                    stream.p1isaac(152);
                    stream.p1(88);
                    anInt1134 = 0;
                }
                stream.p1isaac(21);
                stream.p2(i1);
            }
        }
        if (l == 413) {
            Npc class30_sub2_sub4_sub1_sub1_4 = sessionNpcs[i1];
            if (class30_sub2_sub4_sub1_sub1_4 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_4.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub1_4.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(131);
                stream.isp2(i1);
                stream.sp2(anInt1137);
            }
        }
        if (l == 200)
            clearTopInterfaces();
        if (l == 1025) {
            Npc class30_sub2_sub4_sub1_sub1_5 = sessionNpcs[i1];
            if (class30_sub2_sub4_sub1_sub1_5 != null) {
                NpcDef npcDef = class30_sub2_sub4_sub1_sub1_5.desc;
                if (npcDef.childrenIDs != null)
                    npcDef = npcDef.method161();
                if (npcDef != null) {
                    String s9;
                    if (npcDef.description != null)
                        s9 = new String(npcDef.description);
                    else
                        s9 = "It's a " + npcDef.name + ".";
                    pushMessage(s9, 0, "");
                }
            }
        }
        if (l == 900) {
            clickObject(i1, k, j);
            stream.p1isaac(252);
            stream.isp2(i1 >> 14 & 0x7fff);
            stream.ip2(k + baseY);
            stream.sp2(j + baseX);
        }
        if (l == 412) {
            Npc class30_sub2_sub4_sub1_sub1_6 = sessionNpcs[i1];
            if (class30_sub2_sub4_sub1_sub1_6 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_6.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub1_6.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(72);
                stream.sp2(i1);
            }
        }
        if (l == 365) {
            Player class30_sub2_sub4_sub1_sub2_3 = session_players[i1];
            if (class30_sub2_sub4_sub1_sub2_3 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_3.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub2_3.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(249);
                stream.sp2(i1);
                stream.ip2(anInt1137);
            }
        }
        if (l == 729) {
            Player class30_sub2_sub4_sub1_sub2_4 = session_players[i1];
            if (class30_sub2_sub4_sub1_sub2_4 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_4.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub2_4.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(39);
                stream.ip2(i1);
            }
        }
        if (l == 577) {
            Player class30_sub2_sub4_sub1_sub2_5 = session_players[i1];
            if (class30_sub2_sub4_sub1_sub2_5 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_5.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub2_5.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(139);
                stream.ip2(i1);
            }
        }
        if (l == 956 && clickObject(i1, k, j)) {
            stream.p1isaac(35);
            stream.ip2(j + baseX);
            stream.sp2(anInt1137);
            stream.sp2(k + baseY);
            stream.ip2(i1 >> 14 & 0x7fff);
        }
        if (l == 567) {
            boolean flag6 = doWalkTo(2, 0, 0, 0, session_player.pathY[0], 0, 0, k, session_player.pathX[0], false, j);
            if (!flag6)
                flag6 = doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, k, session_player.pathX[0], false, j);
            crossX = super.clickX;
            crossY = super.clickY;
            crossType = 2;
            crossIndex = 0;
            stream.p1isaac(23);
            stream.ip2(k + baseY);
            stream.ip2(i1);
            stream.ip2(j + baseX);
        }
        if (l == 867) {
            if ((i1 & 3) == 0)
                anInt1175++;
            if (anInt1175 >= 59) {
                stream.p1isaac(200);
                stream.p2(25501);
                anInt1175 = 0;
            }
            stream.p1isaac(43);
            stream.ip2(k);
            stream.sp2(i1);
            stream.sp2(j);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 543) {
            stream.p1isaac(237);
            stream.p2(j);
            stream.sp2(i1);
            stream.p2(k);
            stream.sp2(anInt1137);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 606) {
            String s2 = menuActionName[i];
            int j2 = s2.indexOf("@whi@");
            if (j2 != -1)
                if (openInterfaceID == -1) {
                    clearTopInterfaces();
                    reportAbuseInput = s2.substring(j2 + 5).trim();
                    report_abuse_mute_player = false;
                    for (int i3 = 0; i3 < RSInterface.interfaceCache.length; i3++) {
                        if (RSInterface.interfaceCache[i3] == null || RSInterface.interfaceCache[i3].content_type != 600)
                            continue;
                        reportAbuseInterfaceID = openInterfaceID = RSInterface.interfaceCache[i3].parentID;
                        break;
                    }

                } else {
                    pushMessage("Please close the interface you have open before using 'report abuse'", 0, "");
                }
        }
        if (l == 491) {
            Player class30_sub2_sub4_sub1_sub2_6 = session_players[i1];
            if (class30_sub2_sub4_sub1_sub2_6 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub2_6.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub2_6.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                stream.p1isaac(14);
                stream.sp2(anInt1284);
                stream.p2(i1);
                stream.p2(anInt1285);
                stream.ip2(anInt1283);
            }
        }
        if (l == 639) {
            String s3 = menuActionName[i];
            int k2 = s3.indexOf("@whi@");
            if (k2 != -1) {
                long l4 = TextClass.longForName(s3.substring(k2 + 5).trim());
                int k3 = -1;
                for (int i4 = 0; i4 < user_friends_count; i4++) {
                    if (friendsListAsLongs[i4] != l4)
                        continue;
                    k3 = i4;
                    break;
                }

                if (k3 != -1 && user_friends_worldid[k3] > 0) {
                    inputTaken = true;
                    inputDialogState = 0;
                    messagePromptRaised = true;
                    promptInput = "";
                    friendsListAction = 3;
                    aLong953 = friendsListAsLongs[k3];
                    aString1121 = "Enter message to send to " + user_friends_name_string[k3];
                }
            }
        }
        if (l == 454) {
            stream.p1isaac(41);
            stream.p2(i1);
            stream.sp2(j);
            stream.sp2(k);
            atInventoryLoopCycle = 0;
            atInventoryInterface = k;
            atInventoryIndex = j;
            atInventoryInterfaceType = 2;
            if (RSInterface.interfaceCache[k].parentID == openInterfaceID)
                atInventoryInterfaceType = 1;
            if (RSInterface.interfaceCache[k].parentID == backDialogID)
                atInventoryInterfaceType = 3;
        }
        if (l == 478) {
            Npc class30_sub2_sub4_sub1_sub1_7 = sessionNpcs[i1];
            if (class30_sub2_sub4_sub1_sub1_7 != null) {
                doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, class30_sub2_sub4_sub1_sub1_7.pathY[0], session_player.pathX[0], false, class30_sub2_sub4_sub1_sub1_7.pathX[0]);
                crossX = super.clickX;
                crossY = super.clickY;
                crossType = 2;
                crossIndex = 0;
                if ((i1 & 3) == 0)
                    anInt1155++;
                if (anInt1155 >= 53) {
                    stream.p1isaac(85);
                    stream.p1(66);
                    anInt1155 = 0;
                }
                stream.p1isaac(18);
                stream.ip2(i1);
            }
        }
        if (l == 113) {
            clickObject(i1, k, j);
            stream.p1isaac(70);
            stream.ip2(j + baseX);
            stream.p2(k + baseY);
            stream.isp2(i1 >> 14 & 0x7fff);
        }
        if (l == 872) {
            clickObject(i1, k, j);
            stream.p1isaac(234);
            stream.isp2(j + baseX);
            stream.sp2(i1 >> 14 & 0x7fff);
            stream.isp2(k + baseY);
        }
        if (l == 502) {
            clickObject(i1, k, j);
            stream.p1isaac(132);
            stream.isp2(j + baseX);
            stream.p2(i1 >> 14 & 0x7fff);
            stream.sp2(k + baseY);
        }
        if (l == 1125) {
            ItemDef itemDef = ItemDef.forID(i1);
            RSInterface class9_4 = RSInterface.interfaceCache[k];
            String s5;
            if (class9_4 != null && class9_4.invStackSizes[j] >= 0x186a0)
                s5 = class9_4.invStackSizes[j] + " x " + itemDef.name;
            else if (itemDef.description != null)
                s5 = new String(itemDef.description);
            else
                s5 = "It's a " + itemDef.name + ".";
            pushMessage(s5, 0, "");
        }
        if (l == 169) {
            stream.p1isaac(185);
            stream.p2(k);
            RSInterface class9_3 = RSInterface.interfaceCache[k];
            if (class9_3.dynamic_value_formulas != null && class9_3.dynamic_value_formulas[0][0] == 5) {
                int l2 = class9_3.dynamic_value_formulas[0][1];
                sessionSettings[l2] = 1 - sessionSettings[l2];
                applyConfigChange(l2);
                needDrawTabArea = true;
            }
        }
        if (l == 447) {
            itemSelected = 1;
            anInt1283 = j;
            anInt1284 = k;
            anInt1285 = i1;
            selectedItemName = ItemDef.forID(i1).name;
            spellSelected = 0;
            needDrawTabArea = true;
            return;
        }
        if (l == 1226) {
            int j1 = i1 >> 14 & 0x7fff;
            ObjectDef class46 = ObjectDef.forID(j1);
            String s10;
            if (class46.description != null)
                s10 = new String(class46.description);
            else
                s10 = "It's a " + class46.name + ".";
            pushMessage(s10, 0, "");
        }
        if (l == 244) {
            boolean flag7 = doWalkTo(2, 0, 0, 0, session_player.pathY[0], 0, 0, k, session_player.pathX[0], false, j);
            if (!flag7)
                flag7 = doWalkTo(2, 0, 1, 0, session_player.pathY[0], 1, 0, k, session_player.pathX[0], false, j);
            crossX = super.clickX;
            crossY = super.clickY;
            crossType = 2;
            crossIndex = 0;
            stream.p1isaac(253);
            stream.ip2(j + baseX);
            stream.isp2(k + baseY);
            stream.sp2(i1);
        }
        if (l == 1448) {
            ItemDef itemDef_1 = ItemDef.forID(i1);
            String s6;
            if (itemDef_1.description != null)
                s6 = new String(itemDef_1.description);
            else
                s6 = "It's a " + itemDef_1.name + ".";
            pushMessage(s6, 0, "");
        }
        itemSelected = 0;
        spellSelected = 0;
        needDrawTabArea = true;

    }

    private void checkTutorialIsland()//tutorial island areas
    {
        anInt1251 = 0;
        int bigX = (session_player.boundExtentX >> 7) + baseX;
        int bigY = (session_player.boundExtentY >> 7) + baseY;
        if (bigX >= 3053 && bigX <= 3156 && bigY >= 3056 && bigY <= 3136)
            anInt1251 = 1;
        if (bigX >= 3072 && bigX <= 3118 && bigY >= 9492 && bigY <= 9535)
            anInt1251 = 1;
        if (anInt1251 == 1 && bigX >= 3139 && bigX <= 3199 && bigY >= 3008 && bigY <= 3062)
            anInt1251 = 0;
    }

    public void run() {
        if (drawFlames) {
            drawFlames();
        } else {
            super.run();
        }
    }

    private void build3dScreenMenu() {
        if (itemSelected == 0 && spellSelected == 0) {
            menuActionName[menuActionRow] = "Walk here";
            menuActionID[menuActionRow] = 516;
            menuActionCmd2[menuActionRow] = super.mouseEventX;
            menuActionCmd3[menuActionRow] = super.mouseEventY;
            menuActionRow++;
        }
        int j = -1;
        for (int k = 0; k < Model.resourceCount; k++) {
            int l = Model.resourceIDTAG[k];
            int i1 = l & 0x7f;
            int j1 = l >> 7 & 0x7f;
            int k1 = l >> 29 & 3;
            int l1 = l >> 14 & 0x7fff;
            if (l == j)
                continue;
            j = l;
            if (k1 == 2 && sceneGraph.getIDTAGForXYZ(plane, i1, j1, l) >= 0) {
                ObjectDef class46 = ObjectDef.forID(l1);
                if (class46.configObjectIDs != null)
                    class46 = class46.method580();
                if (class46 == null)
                    continue;
                if (itemSelected == 1) {
                    menuActionName[menuActionRow] = "Use " + selectedItemName + " with @cya@" + class46.name;
                    menuActionID[menuActionRow] = 62;
                    menuActionCmd1[menuActionRow] = l;
                    menuActionCmd2[menuActionRow] = i1;
                    menuActionCmd3[menuActionRow] = j1;
                    menuActionRow++;
                } else if (spellSelected == 1) {
                    if ((spellUsableOn & 4) == 4) {
                        menuActionName[menuActionRow] = spellTooltip + " @cya@" + class46.name;
                        menuActionID[menuActionRow] = 956;
                        menuActionCmd1[menuActionRow] = l;
                        menuActionCmd2[menuActionRow] = i1;
                        menuActionCmd3[menuActionRow] = j1;
                        menuActionRow++;
                    }
                } else {
                    if (class46.actions != null) {
                        for (int i2 = 4; i2 >= 0; i2--)
                            if (class46.actions[i2] != null) {
                                menuActionName[menuActionRow] = class46.actions[i2] + " @cya@" + class46.name;
                                if (i2 == 0)
                                    menuActionID[menuActionRow] = 502;
                                if (i2 == 1)
                                    menuActionID[menuActionRow] = 900;
                                if (i2 == 2)
                                    menuActionID[menuActionRow] = 113;
                                if (i2 == 3)
                                    menuActionID[menuActionRow] = 872;
                                if (i2 == 4)
                                    menuActionID[menuActionRow] = 1062;
                                menuActionCmd1[menuActionRow] = l;
                                menuActionCmd2[menuActionRow] = i1;
                                menuActionCmd3[menuActionRow] = j1;
                                menuActionRow++;
                            }

                    }
                    menuActionName[menuActionRow] = "Examine @cya@" + class46.name + " @gre@(@whi@" + l1 + "@gre@) (@whi@" + (i1 + baseX) + "," + (j1 + baseY) + "@gre@)";
                    menuActionID[menuActionRow] = 1226;
                    menuActionCmd1[menuActionRow] = class46.type << 14;
                    menuActionCmd2[menuActionRow] = i1;
                    menuActionCmd3[menuActionRow] = j1;
                    menuActionRow++;
                }
            }
            if (k1 == 1) {
                Npc npc = sessionNpcs[l1];
                if (npc.desc.aByte68 == 1 && (npc.boundExtentX & 0x7f) == 64 && (npc.boundExtentY & 0x7f) == 64) {
                    for (int j2 = 0; j2 < sessionNpcCount; j2++) {
                        Npc npc2 = sessionNpcs[sessionNpcList[j2]];
                        if (npc2 != null && npc2 != npc && npc2.desc.aByte68 == 1 && npc2.boundExtentX == npc.boundExtentX && npc2.boundExtentY == npc.boundExtentY)
                            buildAtNPCMenu(npc2.desc, sessionNpcList[j2], j1, i1);
                    }

                    for (int l2 = 0; l2 < session_player_count; l2++) {
                        Player player = session_players[session_player_list[l2]];
                        if (player != null && player.boundExtentX == npc.boundExtentX && player.boundExtentY == npc.boundExtentY)
                            buildAtPlayerMenu(i1, session_player_list[l2], player, j1);
                    }

                }
                buildAtNPCMenu(npc.desc, l1, j1, i1);
            }
            if (k1 == 0) {
                Player player = session_players[l1];
                if ((player.boundExtentX & 0x7f) == 64 && (player.boundExtentY & 0x7f) == 64) {
                    for (int k2 = 0; k2 < sessionNpcCount; k2++) {
                        Npc class30_sub2_sub4_sub1_sub1_2 = sessionNpcs[sessionNpcList[k2]];
                        if (class30_sub2_sub4_sub1_sub1_2 != null && class30_sub2_sub4_sub1_sub1_2.desc.aByte68 == 1 && class30_sub2_sub4_sub1_sub1_2.boundExtentX == player.boundExtentX && class30_sub2_sub4_sub1_sub1_2.boundExtentY == player.boundExtentY)
                            buildAtNPCMenu(class30_sub2_sub4_sub1_sub1_2.desc, sessionNpcList[k2], j1, i1);
                    }

                    for (int i3 = 0; i3 < session_player_count; i3++) {
                        Player class30_sub2_sub4_sub1_sub2_2 = session_players[session_player_list[i3]];
                        if (class30_sub2_sub4_sub1_sub2_2 != null && class30_sub2_sub4_sub1_sub2_2 != player && class30_sub2_sub4_sub1_sub2_2.boundExtentX == player.boundExtentX && class30_sub2_sub4_sub1_sub2_2.boundExtentY == player.boundExtentY)
                            buildAtPlayerMenu(i1, session_player_list[i3], class30_sub2_sub4_sub1_sub2_2, j1);
                    }

                }
                buildAtPlayerMenu(i1, l1, player, j1);
            }
            if (k1 == 3) {
                Deque class19 = groundArray[plane][i1][j1];
                if (class19 != null) {
                    for (Item item = (Item) class19.getBack(); item != null; item = (Item) class19.getPrevious()) {
                        ItemDef itemDef = ItemDef.forID(item.ID);
                        if (itemSelected == 1) {
                            menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
                            menuActionID[menuActionRow] = 511;
                            menuActionCmd1[menuActionRow] = item.ID;
                            menuActionCmd2[menuActionRow] = i1;
                            menuActionCmd3[menuActionRow] = j1;
                            menuActionRow++;
                        } else if (spellSelected == 1) {
                            if ((spellUsableOn & 1) == 1) {
                                menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
                                menuActionID[menuActionRow] = 94;
                                menuActionCmd1[menuActionRow] = item.ID;
                                menuActionCmd2[menuActionRow] = i1;
                                menuActionCmd3[menuActionRow] = j1;
                                menuActionRow++;
                            }
                        } else {
                            for (int j3 = 4; j3 >= 0; j3--)
                                if (itemDef.groundActions != null && itemDef.groundActions[j3] != null) {
                                    menuActionName[menuActionRow] = itemDef.groundActions[j3] + " @lre@" + itemDef.name;
                                    if (j3 == 0)
                                        menuActionID[menuActionRow] = 652;
                                    if (j3 == 1)
                                        menuActionID[menuActionRow] = 567;
                                    if (j3 == 2)
                                        menuActionID[menuActionRow] = 234;
                                    if (j3 == 3)
                                        menuActionID[menuActionRow] = 244;
                                    if (j3 == 4)
                                        menuActionID[menuActionRow] = 213;
                                    menuActionCmd1[menuActionRow] = item.ID;
                                    menuActionCmd2[menuActionRow] = i1;
                                    menuActionCmd3[menuActionRow] = j1;
                                    menuActionRow++;
                                } else if (j3 == 2) {
                                    menuActionName[menuActionRow] = "Take @lre@" + itemDef.name;
                                    menuActionID[menuActionRow] = 234;
                                    menuActionCmd1[menuActionRow] = item.ID;
                                    menuActionCmd2[menuActionRow] = i1;
                                    menuActionCmd3[menuActionRow] = j1;
                                    menuActionRow++;
                                }

                            menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + " @gre@(@whi@" + item.ID + "@gre@)";
                            menuActionID[menuActionRow] = 1448;
                            menuActionCmd1[menuActionRow] = item.ID;
                            menuActionCmd2[menuActionRow] = i1;
                            menuActionCmd3[menuActionRow] = j1;
                            menuActionRow++;
                        }
                    }

                }
            }
        }
    }

    public void shutdown() {
        Signlink.reporterror = false;
        try {
            if (socketStream != null)
                socketStream.close();
        } catch (Exception _ex) {
        }
        socketStream = null;
        stopMidi();
        if (mouseDetection != null)
            mouseDetection.running = false;
        mouseDetection = null;
        onDemandFetcher.disable();
        onDemandFetcher = null;
        aStream_834 = null;
        stream = null;
        aStream_847 = null;
        inStream = null;
        mapCoordinates = null;
        terrainData = null;
        aByteArrayArray1247 = null;
        terrainIndices = null;
        anIntArray1236 = null;
        intGroundArray = null;
        tileSettingBits = null;
        sceneGraph = null;
        tileSettings = null;
        anIntArrayArray901 = null;
        anIntArrayArray825 = null;
        bigX = null;
        bigY = null;
        aByteArray912 = null;
        tabAreaDrawingTarget = null;
        minimapIP = null;
        gameScreenCanvas = null;
        aRSImageProducer_1166 = null;
        aRSImageProducer_1123 = null;
        aRSImageProducer_1124 = null;
        aRSImageProducer_1125 = null;
        backLeftIP1 = null;
        backLeftIP2 = null;
        backRightIP1 = null;
        backRightIP2 = null;
        backTopIP1 = null;
        backVmidIP1 = null;
        backVmidIP2 = null;
        backVmidIP3 = null;
        backVmidIP2_2 = null;
        invBack = null;
        mapBack = null;
        chatBack = null;
        backBase1 = null;
        backBase2 = null;
        backHmid1 = null;
        sideIcons = null;
        redStone1 = null;
        redStone2 = null;
        redStone3 = null;
        redStone1_2 = null;
        redStone2_2 = null;
        redStone1_3 = null;
        redStone2_3 = null;
        redStone3_2 = null;
        redStone1_4 = null;
        redStone2_4 = null;
        compass = null;
        hitMarks = null;
        headIcons = null;
        crosses = null;
        mapDotItem = null;
        mapDotNPC = null;
        mapDotPlayer = null;
        mapDotFriend = null;
        mapDotTeam = null;
        mapScenes = null;
        mapFunctions = null;
        anIntArrayArray929 = null;
        session_players = null;
        session_player_list = null;
        session_npcs_awaiting_update = null;
        aStreamArray895s = null;
        anIntArray840 = null;
        sessionNpcs = null;
        sessionNpcList = null;
        groundArray = null;
        aClass19_1179 = null;
        aClass19_1013 = null;
        aClass19_1056 = null;
        menuActionCmd2 = null;
        menuActionCmd3 = null;
        menuActionID = null;
        menuActionCmd1 = null;
        menuActionName = null;
        sessionSettings = null;
        markPosX = null;
        markPosY = null;
        markGraphic = null;
        minimapImage = null;
        user_friends_name_string = null;
        friendsListAsLongs = null;
        user_friends_worldid = null;
        aRSImageProducer_1110 = null;
        aRSImageProducer_1111 = null;
        aRSImageProducer_1107 = null;
        aRSImageProducer_1108 = null;
        aRSImageProducer_1109 = null;
        aRSImageProducer_1112 = null;
        aRSImageProducer_1113 = null;
        aRSImageProducer_1114 = null;
        aRSImageProducer_1115 = null;
        clearCache();
        ObjectDef.clearCache();
        NpcDef.clearCache();
        ItemDef.clearCache();
        Floor.cache = null;
        IdentityKit.cache = null;
        RSInterface.interfaceCache = null;
        Sequence.anims = null;
        SpotAnim.cache = null;
        SpotAnim.memCache = null;
        SettingUsagePointers.cache = null;
        super.fullGameScreen = null;
        Player.memCache = null;
        Rasterizer.clearCache();
        SceneGraph.clearCache();
        Model.clearCache();
        Animation.clearCache();
        System.gc();
    }

    private void printDebug() {
        System.out.println("============");
        System.out.println("flame-cycle:" + anInt1208);
        if (onDemandFetcher != null)
            System.out.println("Od-cycle:" + onDemandFetcher.onDemandCycle);
        System.out.println("loop-cycle:" + currentTime);
        System.out.println("draw-cycle:" + anInt1061);
        System.out.println("ptype:" + pktType);
        System.out.println("psize:" + pktSize);
        if (socketStream != null)
            socketStream.printDebug();
        super.gameShellDumpRequested = true;
    }

    protected Component getGameComponent() {
        if (Signlink.mainapp != null)
            return Signlink.mainapp;
        if (super.gameFrame != null)
            return super.gameFrame;
        else
            return this;
    }

    private void manageTextInputs() {
        do {
            int j = readChar();
            if (j == -1)
                break;
            if (openInterfaceID != -1 && openInterfaceID == reportAbuseInterfaceID) {
                if (j == 8 && reportAbuseInput.length() > 0)
                    reportAbuseInput = reportAbuseInput.substring(0, reportAbuseInput.length() - 1);
                if ((j >= 97 && j <= 122 || j >= 65 && j <= 90 || j >= 48 && j <= 57 || j == 32) && reportAbuseInput.length() < 12)
                    reportAbuseInput += (char) j;
            } else if (messagePromptRaised) {
                if (j >= 32 && j <= 122 && promptInput.length() < 80) {
                    promptInput += (char) j;
                    inputTaken = true;
                }
                if (j == 8 && promptInput.length() > 0) {
                    promptInput = promptInput.substring(0, promptInput.length() - 1);
                    inputTaken = true;
                }
                if (j == 13 || j == 10) {
                    messagePromptRaised = false;
                    inputTaken = true;
                    if (friendsListAction == 1) {
                        long l = TextClass.longForName(promptInput);
                        addFriend(l);
                    }
                    if (friendsListAction == 2 && user_friends_count > 0) {
                        long l1 = TextClass.longForName(promptInput);
                        delFriend(l1);
                    }
                    if (friendsListAction == 3 && promptInput.length() > 0) {
                        stream.p1isaac(126);
                        stream.p1(0);
                        int k = stream.pos;
                        stream.p8(aLong953);
                        TextInput.method526(promptInput, stream);
                        stream.psize1(stream.pos - k);
                        promptInput = TextInput.processText(promptInput);
                        promptInput = Censor.doCensor(promptInput);
                        pushMessage(promptInput, 6, TextClass.fixName(TextClass.nameForLong(aLong953)));
                        if (privateChatMode == 2) {
                            privateChatMode = 1;
                            aBoolean1233 = true;
                            stream.p1isaac(95);
                            stream.p1(publicChatMode);
                            stream.p1(privateChatMode);
                            stream.p1(tradeMode);
                        }
                    }
                    if (friendsListAction == 4 && user_ignore_count < 100) {
                        long l2 = TextClass.longForName(promptInput);
                        addIgnore(l2);
                    }
                    if (friendsListAction == 5 && user_ignore_count > 0) {
                        long l3 = TextClass.longForName(promptInput);
                        delIgnore(l3);
                    }
                }
            } else if (inputDialogState == 1) {
                if (j >= 48 && j <= 57 && amountOrNameInput.length() < 10) {
                    amountOrNameInput += (char) j;
                    inputTaken = true;
                }
                if (j == 8 && amountOrNameInput.length() > 0) {
                    amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
                    inputTaken = true;
                }
                if (j == 13 || j == 10) {
                    if (amountOrNameInput.length() > 0) {
                        int i1 = 0;
                        try {
                            i1 = Integer.parseInt(amountOrNameInput);
                        } catch (Exception _ex) {
                        }
                        stream.p1isaac(208);
                        stream.p4(i1);
                    }
                    inputDialogState = 0;
                    inputTaken = true;
                }
            } else if (inputDialogState == 2) {
                if (j >= 32 && j <= 122 && amountOrNameInput.length() < 12) {
                    amountOrNameInput += (char) j;
                    inputTaken = true;
                }
                if (j == 8 && amountOrNameInput.length() > 0) {
                    amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
                    inputTaken = true;
                }
                if (j == 13 || j == 10) {
                    if (amountOrNameInput.length() > 0) {
                        stream.p1isaac(60);
                        stream.p8(TextClass.longForName(amountOrNameInput));
                    }
                    inputDialogState = 0;
                    inputTaken = true;
                }
            } else if (backDialogID == -1) {
                if (j >= 32 && j <= 122 && inputString.length() < 80) {
                    inputString += (char) j;
                    inputTaken = true;
                }
                if (j == 8 && inputString.length() > 0) {
                    inputString = inputString.substring(0, inputString.length() - 1);
                    inputTaken = true;
                }
                if ((j == 13 || j == 10) && inputString.length() > 0) {

                    if (inputString.equals("::noclip")) {
                        for (int k1 = 0; k1 < 4; k1++) {
                            for (int i2 = 1; i2 < 103; i2++) {
                                for (int k2 = 1; k2 < 103; k2++)
                                    tileSettings[k1].clipData[i2][k2] = 0;

                            }

                        }

                    }
                    //if(myPrivilege == 2)
                    //{
                    if (inputString.equals("::clientdrop"))
                        dropClient();
                    if (inputString.equals("::lag"))
                        printDebug();
                    if (inputString.equals("::lighting"))
                        Class7_Sub1.useLighting = !Class7_Sub1.useLighting;
                    if (inputString.equals("::prefetchmusic")) {
                        for (int j1 = 0; j1 < onDemandFetcher.getVersionCount(2); j1++)
                            onDemandFetcher.method563((byte) 1, 2, j1);

                    }
                    if (inputString.equals("::fpson"))
                        fpsOn = true;
                    if (inputString.equals("::fpsoff"))
                        fpsOn = false;

                    //}
                    if (inputString.startsWith("::") && !inputString.startsWith("::nocl")) {
                        stream.p1isaac(103);
                        stream.p1(inputString.length() - 1);
                        stream.pjstr(inputString.substring(2));
                    } else {
                        String s = inputString.toLowerCase();
                        int j2 = 0;
                        if (s.startsWith("yellow:")) {
                            j2 = 0;
                            inputString = inputString.substring(7);
                        } else if (s.startsWith("red:")) {
                            j2 = 1;
                            inputString = inputString.substring(4);
                        } else if (s.startsWith("green:")) {
                            j2 = 2;
                            inputString = inputString.substring(6);
                        } else if (s.startsWith("cyan:")) {
                            j2 = 3;
                            inputString = inputString.substring(5);
                        } else if (s.startsWith("purple:")) {
                            j2 = 4;
                            inputString = inputString.substring(7);
                        } else if (s.startsWith("white:")) {
                            j2 = 5;
                            inputString = inputString.substring(6);
                        } else if (s.startsWith("flash1:")) {
                            j2 = 6;
                            inputString = inputString.substring(7);
                        } else if (s.startsWith("flash2:")) {
                            j2 = 7;
                            inputString = inputString.substring(7);
                        } else if (s.startsWith("flash3:")) {
                            j2 = 8;
                            inputString = inputString.substring(7);
                        } else if (s.startsWith("glow1:")) {
                            j2 = 9;
                            inputString = inputString.substring(6);
                        } else if (s.startsWith("glow2:")) {
                            j2 = 10;
                            inputString = inputString.substring(6);
                        } else if (s.startsWith("glow3:")) {
                            j2 = 11;
                            inputString = inputString.substring(6);
                        }
                        s = inputString.toLowerCase();
                        int i3 = 0;
                        if (s.startsWith("wave:")) {
                            i3 = 1;
                            inputString = inputString.substring(5);
                        } else if (s.startsWith("wave2:")) {
                            i3 = 2;
                            inputString = inputString.substring(6);
                        } else if (s.startsWith("shake:")) {
                            i3 = 3;
                            inputString = inputString.substring(6);
                        } else if (s.startsWith("scroll:")) {
                            i3 = 4;
                            inputString = inputString.substring(7);
                        } else if (s.startsWith("slide:")) {
                            i3 = 5;
                            inputString = inputString.substring(6);
                        }
                        stream.p1isaac(4);
                        stream.p1(0);
                        int j3 = stream.pos;
                        stream.sp1(i3);
                        stream.sp1(j2);
                        aStream_834.pos = 0;
                        TextInput.method526(inputString, aStream_834);
                        stream.ispdata(aStream_834.data, 0, aStream_834.pos);
                        stream.psize1(stream.pos - j3);
                        inputString = TextInput.processText(inputString);
                        inputString = Censor.doCensor(inputString);
                        session_player.textSpoken = inputString;
                        session_player.anInt1513 = j2;
                        session_player.anInt1531 = i3;
                        session_player.textCycle = 150;
                        if (myPrivilege == 2)
                            pushMessage(session_player.textSpoken, 2, "@cr2@" + session_player.name);
                        else if (myPrivilege == 1)
                            pushMessage(session_player.textSpoken, 2, "@cr1@" + session_player.name);
                        else
                            pushMessage(session_player.textSpoken, 2, session_player.name);
                        if (publicChatMode == 2) {
                            publicChatMode = 3;
                            aBoolean1233 = true;
                            stream.p1isaac(95);
                            stream.p1(publicChatMode);
                            stream.p1(privateChatMode);
                            stream.p1(tradeMode);
                        }
                    }
                    inputString = "";
                    inputTaken = true;
                }
            }
        } while (true);
    }

    private void buildChatAreaMenu(int j) {
        int l = 0;
        for (int i1 = 0; i1 < 100; i1++) {
            if (chatMessages[i1] == null)
                continue;
            int j1 = chatTypes[i1];
            int k1 = (70 - l * 14) + anInt1089 + 4;
            if (k1 < -20)
                break;
            String s = chatNames[i1];
            //boolean flag = false;//never used
            if (s != null && s.startsWith("@cr1@")) {
                s = s.substring(5);
                //boolean flag1 = true;//never used
            }
            if (s != null && s.startsWith("@cr2@")) {
                s = s.substring(5);
                //byte byte0 = 2;//never used
            }
            if (j1 == 0)
                l++;
            if ((j1 == 1 || j1 == 2) && (j1 == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s))) {
                if (j > k1 - 14 && j <= k1 && !s.equals(session_player.name)) {
                    if (myPrivilege >= 1) {
                        menuActionName[menuActionRow] = "Report abuse @whi@" + s;
                        menuActionID[menuActionRow] = 606;
                        menuActionRow++;
                    }
                    menuActionName[menuActionRow] = "Add ignore @whi@" + s;
                    menuActionID[menuActionRow] = 42;
                    menuActionRow++;
                    menuActionName[menuActionRow] = "Add friend @whi@" + s;
                    menuActionID[menuActionRow] = 337;
                    menuActionRow++;
                }
                l++;
            }
            if ((j1 == 3 || j1 == 7) && ui_split_private_chat == 0 && (j1 == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
                if (j > k1 - 14 && j <= k1) {
                    if (myPrivilege >= 1) {
                        menuActionName[menuActionRow] = "Report abuse @whi@" + s;
                        menuActionID[menuActionRow] = 606;
                        menuActionRow++;
                    }
                    menuActionName[menuActionRow] = "Add ignore @whi@" + s;
                    menuActionID[menuActionRow] = 42;
                    menuActionRow++;
                    menuActionName[menuActionRow] = "Add friend @whi@" + s;
                    menuActionID[menuActionRow] = 337;
                    menuActionRow++;
                }
                l++;
            }
            if (j1 == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
                if (j > k1 - 14 && j <= k1) {
                    menuActionName[menuActionRow] = "Accept trade @whi@" + s;
                    menuActionID[menuActionRow] = 484;
                    menuActionRow++;
                }
                l++;
            }
            if ((j1 == 5 || j1 == 6) && ui_split_private_chat == 0 && privateChatMode < 2)
                l++;
            if (j1 == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
                if (j > k1 - 14 && j <= k1) {
                    menuActionName[menuActionRow] = "Accept challenge @whi@" + s;
                    menuActionID[menuActionRow] = 6;
                    menuActionRow++;
                }
                l++;
            }
        }

    }

    private void interface_handle_auto_content(RSInterface class9) {
        int j = class9.content_type;
        if (j >= 1 && j <= 100 || j >= 701 && j <= 800) {
            if (j == 1 && network_friends_server_status == 0) {
                class9.text_conditionfalse = "Loading friend list";
                class9.atActionType = 0;
                return;
            }
            if (j == 1 && network_friends_server_status == 1) {
                class9.text_conditionfalse = "Connecting to friendserver";
                class9.atActionType = 0;
                return;
            }
            if (j == 2 && network_friends_server_status != 2) {
                class9.text_conditionfalse = "Please wait...";
                class9.atActionType = 0;
                return;
            }
            int k = user_friends_count;
            if (network_friends_server_status != 2)
                k = 0;
            if (j > 700)
                j -= 601;
            else
                j--;
            if (j >= k) {
                class9.text_conditionfalse = "";
                class9.atActionType = 0;
                return;
            } else {
                class9.text_conditionfalse = user_friends_name_string[j];
                class9.atActionType = 1;
                return;
            }
        }
        if (j >= 101 && j <= 200 || j >= 801 && j <= 900) {
            int l = user_friends_count;
            if (network_friends_server_status != 2)
                l = 0;
            if (j > 800)
                j -= 701;
            else
                j -= 101;
            if (j >= l) {
                class9.text_conditionfalse = "";
                class9.atActionType = 0;
                return;
            }
            if (user_friends_worldid[j] == 0)
                class9.text_conditionfalse = "@red@Offline";
            else if (user_friends_worldid[j] == network_worldid)
                class9.text_conditionfalse = "@gre@World-" + (user_friends_worldid[j] - 9);
            else
                class9.text_conditionfalse = "@yel@World-" + (user_friends_worldid[j] - 9);
            class9.atActionType = 1;
            return;
        }
        if (j == 203) {
            int i1 = user_friends_count;
            if (network_friends_server_status != 2)
                i1 = 0;
            class9.scrollMax = i1 * 15 + 20;
            if (class9.scrollMax <= class9.height)
                class9.scrollMax = class9.height + 1;
            return;
        }
        if (j >= 401 && j <= 500) {
            if ((j -= 401) == 0 && network_friends_server_status == 0) {
                class9.text_conditionfalse = "Loading ignore list";
                class9.atActionType = 0;
                return;
            }
            if (j == 1 && network_friends_server_status == 0) {
                class9.text_conditionfalse = "Please wait...";
                class9.atActionType = 0;
                return;
            }
            int j1 = user_ignore_count;
            if (network_friends_server_status == 0)
                j1 = 0;
            if (j >= j1) {
                class9.text_conditionfalse = "";
                class9.atActionType = 0;
                return;
            } else {
                class9.text_conditionfalse = TextClass.fixName(TextClass.nameForLong(user_ignore_names[j]));
                class9.atActionType = 1;
                return;
            }
        }
        if (j == 503) {
            class9.scrollMax = user_ignore_count * 15 + 20;
            if (class9.scrollMax <= class9.height)
                class9.scrollMax = class9.height + 1;
            return;
        }
        if (j == 327) {
            class9.anInt270 = 150;
            class9.anInt271 = (int) (Math.sin((double) currentTime / 40D) * 256D) & 0x7ff;
            if (char_edit_model_changed) {
                for (int idkit_ptr = 0; idkit_ptr < 7; idkit_ptr++) {
                    int idkit_id = char_edit_idkits[idkit_ptr];
                    if (idkit_id >= 0 && !IdentityKit.cache[idkit_id].isBodyDownloaded())
                        return;
                }

                char_edit_model_changed = false;
                Model sub_models[] = new Model[7];
                int model_ptr = 0;
                for (int idkit_ptr = 0; idkit_ptr < 7; idkit_ptr++) {
                    int idkit_id = char_edit_idkits[idkit_ptr];
                    if (idkit_id >= 0)
                        sub_models[model_ptr++] = IdentityKit.cache[idkit_id].getBodyModel();
                }

                Model model = new Model(model_ptr, sub_models);
                for (int idkit_ptr = 0; idkit_ptr < 5; idkit_ptr++)
                    if (char_edit_colours[idkit_ptr] != 0) {
                        model.recolour(playerBodyRecolours[idkit_ptr][0], playerBodyRecolours[idkit_ptr][char_edit_colours[idkit_ptr]]);
                        if (idkit_ptr == 1)
                            model.recolour(skinColours[0], skinColours[char_edit_colours[idkit_ptr]]);
                    }

                model.calcSkinning();
                model.applyTransform(Sequence.anims[session_player.anInt1511].frame2IDS[0]);
                model.light(64, 850, -30, -50, -30, true);
                class9.anInt233 = 5;
                class9.mediaID = 0;
                RSInterface.set_custom_model(aBoolean994, model);
            }
            return;
        }
        if (j == 324) {
            if (char_edit_inactive_button == null) {
                char_edit_inactive_button = class9.image_conditionfalse;
                char_edit_active_button = class9.image_conditiontrue;
            }
            if (char_edit_gender) {
                class9.image_conditionfalse = char_edit_active_button;
                return;
            } else {
                class9.image_conditionfalse = char_edit_inactive_button;
                return;
            }
        }
        if (j == 325) {
            if (char_edit_inactive_button == null) {
                char_edit_inactive_button = class9.image_conditionfalse;
                char_edit_active_button = class9.image_conditiontrue;
            }
            if (char_edit_gender) {
                class9.image_conditionfalse = char_edit_inactive_button;
                return;
            } else {
                class9.image_conditionfalse = char_edit_active_button;
                return;
            }
        }
        if (j == 600) {
            class9.text_conditionfalse = reportAbuseInput;
            if (currentTime % 20 < 10) {
                class9.text_conditionfalse += "|";
                return;
            } else {
                class9.text_conditionfalse += " ";
                return;
            }
        }
        if (j == 613)
            if (myPrivilege >= 1) {
                if (report_abuse_mute_player) {
                    class9.colour_conditionfalse = 0xff0000;
                    class9.text_conditionfalse = "Moderator option: Mute player for 48 hours: <ON>";
                } else {
                    class9.colour_conditionfalse = 0xffffff;
                    class9.text_conditionfalse = "Moderator option: Mute player for 48 hours: <OFF>";
                }
            } else {
                class9.text_conditionfalse = "";
            }
        if (j == 650 || j == 655)
            if (anInt1193 != 0) {
                String s;
                if (welcome_screen_last_login == 0)
                    s = "earlier today";
                else if (welcome_screen_last_login == 1)
                    s = "yesterday";
                else
                    s = welcome_screen_last_login + " days ago";
                class9.text_conditionfalse = "You last logged in " + s + " from: " + Signlink.dns;
            } else {
                class9.text_conditionfalse = "";
            }
        if (j == 651) {
            if (welcome_screen_unread_pm_count == 0) {
                class9.text_conditionfalse = "0 unread messages";
                class9.colour_conditionfalse = 0xffff00;
            }
            if (welcome_screen_unread_pm_count == 1) {
                class9.text_conditionfalse = "1 unread message";
                class9.colour_conditionfalse = 65280;
            }
            if (welcome_screen_unread_pm_count > 1) {
                class9.text_conditionfalse = welcome_screen_unread_pm_count + " unread messages";
                class9.colour_conditionfalse = 65280;
            }
        }
        if (j == 652)
            if (welcome_screen_last_recovery_change_or_member_warning == 201) {
                if (membersInt == 1)
                    class9.text_conditionfalse = "@yel@This is a non-members world: @whi@Since you are a member we";
                else
                    class9.text_conditionfalse = "";
            } else if (welcome_screen_last_recovery_change_or_member_warning == 200) {
                class9.text_conditionfalse = "You have not yet set any password recovery questions.";
            } else {
                String s1;
                if (welcome_screen_last_recovery_change_or_member_warning == 0)
                    s1 = "Earlier today";
                else if (welcome_screen_last_recovery_change_or_member_warning == 1)
                    s1 = "Yesterday";
                else
                    s1 = welcome_screen_last_recovery_change_or_member_warning + " days ago";
                class9.text_conditionfalse = s1 + " you changed your recovery questions";
            }
        if (j == 653)
            if (welcome_screen_last_recovery_change_or_member_warning == 201) {
                if (membersInt == 1)
                    class9.text_conditionfalse = "@whi@recommend you use a members world instead. You may use";
                else
                    class9.text_conditionfalse = "";
            } else if (welcome_screen_last_recovery_change_or_member_warning == 200)
                class9.text_conditionfalse = "We strongly recommend you do so now to secure your account.";
            else
                class9.text_conditionfalse = "If you do not remember making this change then cancel it immediately";
        if (j == 654) {
            if (welcome_screen_last_recovery_change_or_member_warning == 201)
                if (membersInt == 1) {
                    class9.text_conditionfalse = "@whi@this world but member benefits are unavailable whilst here.";
                    return;
                } else {
                    class9.text_conditionfalse = "";
                    return;
                }
            if (welcome_screen_last_recovery_change_or_member_warning == 200) {
                class9.text_conditionfalse = "Do this from the 'account management' area on our front webpage";
                return;
            }
            class9.text_conditionfalse = "Do this from the 'account management' area on our front webpage";
        }
    }

    private void drawSplitPrivateChat() {
        if (ui_split_private_chat == 0)
            return;
        RSFont RSFont = plainFont;
        int i = 0;
        if (anInt1104 != 0)
            i = 1;
        for (int j = 0; j < 100; j++)
            if (chatMessages[j] != null) {
                int k = chatTypes[j];
                String s = chatNames[j];
                byte byte1 = 0;
                if (s != null && s.startsWith("@cr1@")) {
                    s = s.substring(5);
                    byte1 = 1;
                }
                if (s != null && s.startsWith("@cr2@")) {
                    s = s.substring(5);
                    byte1 = 2;
                }
                if ((k == 3 || k == 7) && (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
                    int l = 329 - i * 13;
                    int k1 = 4;
                    RSFont.drawTextHLeftVMid(0, "From", l, k1);
                    RSFont.drawTextHLeftVMid(65535, "From", l - 1, k1);
                    k1 += RSFont.getTextWidth("From ");
                    if (byte1 == 1) {
                        modIcons[0].drawImage(k1, l - 12);
                        k1 += 14;
                    }
                    if (byte1 == 2) {
                        modIcons[1].drawImage(k1, l - 12);
                        k1 += 14;
                    }
                    RSFont.drawTextHLeftVMid(0, s + ": " + chatMessages[j], l, k1);
                    RSFont.drawTextHLeftVMid(65535, s + ": " + chatMessages[j], l - 1, k1);
                    if (++i >= 5)
                        return;
                }
                if (k == 5 && privateChatMode < 2) {
                    int i1 = 329 - i * 13;
                    RSFont.drawTextHLeftVMid(0, chatMessages[j], i1, 4);
                    RSFont.drawTextHLeftVMid(65535, chatMessages[j], i1 - 1, 4);
                    if (++i >= 5)
                        return;
                }
                if (k == 6 && privateChatMode < 2) {
                    int j1 = 329 - i * 13;
                    RSFont.drawTextHLeftVMid(0, "To " + s + ": " + chatMessages[j], j1, 4);
                    RSFont.drawTextHLeftVMid(65535, "To " + s + ": " + chatMessages[j], j1 - 1, 4);
                    if (++i >= 5)
                        return;
                }
            }

    }

    private void pushMessage(String s, int i, String s1) {
        if (i == 0 && dialogID != -1) {
            aString844 = s;
            super.mouseButtonPressed = 0;
        }
        if (backDialogID == -1)
            inputTaken = true;
        for (int j = 99; j > 0; j--) {
            chatTypes[j] = chatTypes[j - 1];
            chatNames[j] = chatNames[j - 1];
            chatMessages[j] = chatMessages[j - 1];
        }

        chatTypes[0] = i;
        chatNames[0] = s1;
        chatMessages[0] = s;
    }

    private void processTabClick() {
        if (super.mouseButtonPressed == 1) {
            if (super.clickX >= 539 && super.clickX <= 573 && super.clickY >= 169 && super.clickY < 205 && tabInterfaceIDs[0] != -1) {
                needDrawTabArea = true;
                tabID = 0;
                tabAreaAltered = true;
            }
            if (super.clickX >= 569 && super.clickX <= 599 && super.clickY >= 168 && super.clickY < 205 && tabInterfaceIDs[1] != -1) {
                needDrawTabArea = true;
                tabID = 1;
                tabAreaAltered = true;
            }
            if (super.clickX >= 597 && super.clickX <= 627 && super.clickY >= 168 && super.clickY < 205 && tabInterfaceIDs[2] != -1) {
                needDrawTabArea = true;
                tabID = 2;
                tabAreaAltered = true;
            }
            if (super.clickX >= 625 && super.clickX <= 669 && super.clickY >= 168 && super.clickY < 203 && tabInterfaceIDs[3] != -1) {
                needDrawTabArea = true;
                tabID = 3;
                tabAreaAltered = true;
            }
            if (super.clickX >= 666 && super.clickX <= 696 && super.clickY >= 168 && super.clickY < 205 && tabInterfaceIDs[4] != -1) {
                needDrawTabArea = true;
                tabID = 4;
                tabAreaAltered = true;
            }
            if (super.clickX >= 694 && super.clickX <= 724 && super.clickY >= 168 && super.clickY < 205 && tabInterfaceIDs[5] != -1) {
                needDrawTabArea = true;
                tabID = 5;
                tabAreaAltered = true;
            }
            if (super.clickX >= 722 && super.clickX <= 756 && super.clickY >= 169 && super.clickY < 205 && tabInterfaceIDs[6] != -1) {
                needDrawTabArea = true;
                tabID = 6;
                tabAreaAltered = true;
            }
            if (super.clickX >= 540 && super.clickX <= 574 && super.clickY >= 466 && super.clickY < 502 && tabInterfaceIDs[7] != -1) {
                needDrawTabArea = true;
                tabID = 7;
                tabAreaAltered = true;
            }
            if (super.clickX >= 572 && super.clickX <= 602 && super.clickY >= 466 && super.clickY < 503 && tabInterfaceIDs[8] != -1) {
                needDrawTabArea = true;
                tabID = 8;
                tabAreaAltered = true;
            }
            if (super.clickX >= 599 && super.clickX <= 629 && super.clickY >= 466 && super.clickY < 503 && tabInterfaceIDs[9] != -1) {
                needDrawTabArea = true;
                tabID = 9;
                tabAreaAltered = true;
            }
            if (super.clickX >= 627 && super.clickX <= 671 && super.clickY >= 467 && super.clickY < 502 && tabInterfaceIDs[10] != -1) {
                needDrawTabArea = true;
                tabID = 10;
                tabAreaAltered = true;
            }
            if (super.clickX >= 669 && super.clickX <= 699 && super.clickY >= 466 && super.clickY < 503 && tabInterfaceIDs[11] != -1) {
                needDrawTabArea = true;
                tabID = 11;
                tabAreaAltered = true;
            }
            if (super.clickX >= 696 && super.clickX <= 726 && super.clickY >= 466 && super.clickY < 503 && tabInterfaceIDs[12] != -1) {
                needDrawTabArea = true;
                tabID = 12;
                tabAreaAltered = true;
            }
            if (super.clickX >= 724 && super.clickX <= 758 && super.clickY >= 466 && super.clickY < 502 && tabInterfaceIDs[13] != -1) {
                needDrawTabArea = true;
                tabID = 13;
                tabAreaAltered = true;
            }
        }
    }

    private void resetImageProducers2() {
        if (aRSImageProducer_1166 != null)
            return;
        clearCache();
        super.fullGameScreen = null;
        aRSImageProducer_1107 = null;
        aRSImageProducer_1108 = null;
        aRSImageProducer_1109 = null;
        aRSImageProducer_1110 = null;
        aRSImageProducer_1111 = null;
        aRSImageProducer_1112 = null;
        aRSImageProducer_1113 = null;
        aRSImageProducer_1114 = null;
        aRSImageProducer_1115 = null;
        aRSImageProducer_1166 = new GraphicsBuffer(479, 96, getGameComponent());
        minimapIP = new GraphicsBuffer(172, 156, getGameComponent());
        Graphics2D.resetImage();
        mapBack.drawImage(0, 0);
        tabAreaDrawingTarget = new GraphicsBuffer(190, 261, getGameComponent());
        gameScreenCanvas = new GraphicsBuffer(512, 334, getGameComponent());
        Graphics2D.resetImage();
        aRSImageProducer_1123 = new GraphicsBuffer(496, 50, getGameComponent());
        aRSImageProducer_1124 = new GraphicsBuffer(269, 37, getGameComponent());
        aRSImageProducer_1125 = new GraphicsBuffer(249, 45, getGameComponent());
        repaintRequested = true;
    }

    private String getDocumentBaseHost() {
        if (Signlink.mainapp != null)
            return Signlink.mainapp.getDocumentBase().getHost().toLowerCase();
        if (super.gameFrame != null)
            return "";
        else
            return "";
    }

    private void method81(RgbImage rgbImage, int j, int k)//todo - is this that white line thing?
    {
        int l = k * k + j * j;
        if (l > 4225 && l < 0x15f90) {
            int i1 = minimapInt1 + minimapInt2 & 0x7ff;
            int j1 = Model.SINE[i1];
            int k1 = Model.COSINE[i1];
            j1 = (j1 * 256) / (minimapInt3 + 256);
            k1 = (k1 * 256) / (minimapInt3 + 256);
            int l1 = j * j1 + k * k1 >> 16;
            int i2 = j * k1 - k * j1 >> 16;
            double d = Math.atan2(l1, i2);
            int j2 = (int) (Math.sin(d) * 63D);
            int k2 = (int) (Math.cos(d) * 57D);
            mapEdge.rotate(83 - k2 - 20, d, (94 + j2 + 4) - 10);
        } else {
            markMinimap(rgbImage, k, j);
        }
    }

    private void processRightClick() {
        if (activeInterfaceType != 0)
            return;
        menuActionName[0] = "Cancel";
        menuActionID[0] = 1107;
        menuActionRow = 1;
        buildSplitPrivateChatMenu();
        anInt886 = 0;
        if (super.mouseEventX > 4 && super.mouseEventY > 4 && super.mouseEventX < 516 && super.mouseEventY < 338)
            if (openInterfaceID != -1)
                buildInterfaceMenu(4, RSInterface.interfaceCache[openInterfaceID], super.mouseEventX, 4, super.mouseEventY, 0);
            else
                build3dScreenMenu();
        if (anInt886 != anInt1026)
            anInt1026 = anInt886;
        anInt886 = 0;
        if (super.mouseEventX > 553 && super.mouseEventY > 205 && super.mouseEventX < 743 && super.mouseEventY < 466)
            if (invOverlayInterfaceID != -1)
                buildInterfaceMenu(553, RSInterface.interfaceCache[invOverlayInterfaceID], super.mouseEventX, 205, super.mouseEventY, 0);
            else if (tabInterfaceIDs[tabID] != -1)
                buildInterfaceMenu(553, RSInterface.interfaceCache[tabInterfaceIDs[tabID]], super.mouseEventX, 205, super.mouseEventY, 0);
        if (anInt886 != anInt1048) {
            needDrawTabArea = true;
            anInt1048 = anInt886;
        }
        anInt886 = 0;
        if (super.mouseEventX > 17 && super.mouseEventY > 357 && super.mouseEventX < 496 && super.mouseEventY < 453)
            if (backDialogID != -1)
                buildInterfaceMenu(17, RSInterface.interfaceCache[backDialogID], super.mouseEventX, 357, super.mouseEventY, 0);
            else if (super.mouseEventY < 434 && super.mouseEventX < 426)
                buildChatAreaMenu(super.mouseEventY - 357);
        if (backDialogID != -1 && anInt886 != anInt1039) {
            inputTaken = true;
            anInt1039 = anInt886;
        }
        boolean flag = false;
        while (!flag) {
            flag = true;
            for (int j = 0; j < menuActionRow - 1; j++)
                if (menuActionID[j] < 1000 && menuActionID[j + 1] > 1000) {
                    String s = menuActionName[j];
                    menuActionName[j] = menuActionName[j + 1];
                    menuActionName[j + 1] = s;
                    int k = menuActionID[j];
                    menuActionID[j] = menuActionID[j + 1];
                    menuActionID[j + 1] = k;
                    k = menuActionCmd2[j];
                    menuActionCmd2[j] = menuActionCmd2[j + 1];
                    menuActionCmd2[j + 1] = k;
                    k = menuActionCmd3[j];
                    menuActionCmd3[j] = menuActionCmd3[j + 1];
                    menuActionCmd3[j + 1] = k;
                    k = menuActionCmd1[j];
                    menuActionCmd1[j] = menuActionCmd1[j + 1];
                    menuActionCmd1[j + 1] = k;
                    flag = false;
                }

        }
    }

    private int rotateFlameColour(int r, int g, int b) {
        int l = 256 - b;
        return ((r & 0xff00ff) * l + (g & 0xff00ff) * b & 0xff00ff00) + ((r & 0xff00) * l + (g & 0xff00) * b & 0xff0000) >> 8;
    }

    private void login(String s, String s1, boolean flag)//flag = reconnecting?
    {


        Signlink.errorname = s;
        try {
            if (!flag) {
                loginMessage1 = "";
                loginMessage2 = "Connecting to server...";
                drawLoginScreen(true, 1);
            }
            socketStream = new RSSocket(this, openSocket(43594 + portOff));
            long l = TextClass.longForName(s);
            int i = (int) (l >> 16 & 31L);
            stream.pos = 0;
            stream.p1(14);
            stream.p1(i);
            socketStream.queueBytes(2, stream.data);
            for (int j = 0; j < 8; j++)
                socketStream.read();

            int k = socketStream.read();
            int i1 = k;
            if (k == 0) {
                socketStream.flushInputStream(inStream.data, 8);
                inStream.pos = 0;
                aLong1215 = inStream.g8();
                int ai[] = new int[4];
                ai[0] = (int) (Math.random() * 99999999D);
                ai[1] = (int) (Math.random() * 99999999D);
                ai[2] = (int) (aLong1215 >> 32);
                ai[3] = (int) aLong1215;
                stream.pos = 0;
                stream.p1(10);
                stream.p4(ai[0]);
                stream.p4(ai[1]);
                stream.p4(ai[2]);
                stream.p4(ai[3]);
                stream.p4(Signlink.uid);
                stream.pjstr(s);
                stream.pjstr(s1);
                stream.rsaenc();
                aStream_847.pos = 0;
                if (flag)
                    aStream_847.p1(18);
                else
                    aStream_847.p1(16);
                aStream_847.p1(stream.pos + 36 + 1 + 1 + 2);
                aStream_847.p1(255);
                aStream_847.p2(317);
                aStream_847.p1(lowMem ? 1 : 0);
                for (int l1 = 0; l1 < 9; l1++)
                    aStream_847.p4(expectedCRCs[l1]);

                aStream_847.pdata(stream.data, 0, stream.pos);
                stream.encryption = new IsaacCipher(ai);
                for (int j2 = 0; j2 < 4; j2++)
                    ai[j2] += 50;

                encryption = new IsaacCipher(ai);
                socketStream.queueBytes(aStream_847.pos, aStream_847.data);
                k = socketStream.read();
            }
            if (k == 1) {
                try {
                    Thread.sleep(2000L);
                } catch (Exception _ex) {
                }
                login(s, s1, flag);
                return;
            }
            if (k == 2) {
                myPrivilege = socketStream.read();
                flagged = socketStream.read() == 1;
                aLong1220 = 0L;
                anInt1022 = 0;
                mouseDetection.coordsIndex = 0;
                super.awtFocus = true;
                aBoolean954 = true;
                loggedIn = true;
                stream.pos = 0;
                inStream.pos = 0;
                pktType = -1;
                anInt841 = -1;
                anInt842 = -1;
                anInt843 = -1;
                pktSize = 0;
                anInt1009 = 0;
                anInt1104 = 0;
                anInt1011 = 0;
                anInt855 = 0;
                menuActionRow = 0;
                menuOpen = false;
                super.idleTime = 0;
                for (int j1 = 0; j1 < 100; j1++)
                    chatMessages[j1] = null;

                itemSelected = 0;
                spellSelected = 0;
                loadingStage = 0;
                anInt1062 = 0;
                anInt1278 = (int) (Math.random() * 100D) - 50;
                anInt1131 = (int) (Math.random() * 110D) - 55;
                anInt896 = (int) (Math.random() * 80D) - 40;
                minimapInt2 = (int) (Math.random() * 120D) - 60;
                minimapInt3 = (int) (Math.random() * 30D) - 20;
                minimapInt1 = (int) (Math.random() * 20D) - 10 & 0x7ff;
                miniMapLock = 0;
                anInt985 = -1;
                destX = 0;
                destY = 0;
                session_player_count = 0;
                sessionNpcCount = 0;
                for (int i2 = 0; i2 < maxPlayers; i2++) {
                    session_players[i2] = null;
                    aStreamArray895s[i2] = null;
                }

                for (int k2 = 0; k2 < 16384; k2++)
                    sessionNpcs[k2] = null;

                session_player = session_players[session_player_idx] = new Player();
                aClass19_1013.clear();
                aClass19_1056.clear();
                for (int l2 = 0; l2 < 4; l2++) {
                    for (int i3 = 0; i3 < 104; i3++) {
                        for (int k3 = 0; k3 < 104; k3++)
                            groundArray[l2][i3][k3] = null;

                    }

                }

                aClass19_1179 = new Deque();
                network_friends_server_status = 0;
                user_friends_count = 0;
                dialogID = -1;
                backDialogID = -1;
                openInterfaceID = -1;
                invOverlayInterfaceID = -1;
                anInt1018 = -1;
                aBoolean1149 = false;
                tabID = 3;
                inputDialogState = 0;
                menuOpen = false;
                messagePromptRaised = false;
                aString844 = null;
                anInt1055 = 0;
                anInt1054 = -1;
                char_edit_gender = true;
                char_edit_change_gender();
                for (int j3 = 0; j3 < 5; j3++)
                    char_edit_colours[j3] = 0;

                for (int l3 = 0; l3 < 5; l3++) {
                    atPlayerActions[l3] = null;
                    atPlayerArray[l3] = false;
                }

                anInt1175 = 0;
                anInt1134 = 0;
                anInt986 = 0;
                anInt1288 = 0;
                anInt924 = 0;
                anInt1188 = 0;
                anInt1155 = 0;
                anInt1226 = 0;
                //int anInt941 = 0;//never used
                //int anInt1260 = 0;//never used
                resetImageProducers2();
                return;
            }
            if (k == 3) {
                loginMessage1 = "";
                loginMessage2 = "Invalid username or password.";
                return;
            }
            if (k == 4) {
                loginMessage1 = "Your account has been disabled.";
                loginMessage2 = "Please check your message-center for details.";
                return;
            }
            if (k == 5) {
                loginMessage1 = "Your account is already logged in.";
                loginMessage2 = "Try again in 60 secs...";
                return;
            }
            if (k == 6) {
                loginMessage1 = "RuneScape has been updated!";
                loginMessage2 = "Please reload this page.";
                return;
            }
            if (k == 7) {
                loginMessage1 = "This world is full.";
                loginMessage2 = "Please use a different world.";
                return;
            }
            if (k == 8) {
                loginMessage1 = "Unable to connect.";
                loginMessage2 = "Login server offline.";
                return;
            }
            if (k == 9) {
                loginMessage1 = "Login limit exceeded.";
                loginMessage2 = "Too many connections from your address.";
                return;
            }
            if (k == 10) {
                loginMessage1 = "Unable to connect.";
                loginMessage2 = "Bad session hash.";
                return;
            }
            if (k == 11) {
                loginMessage2 = "Login server rejected session.";
                loginMessage2 = "Please try again.";
                return;
            }
            if (k == 12) {
                loginMessage1 = "You need a members account to login to this world.";
                loginMessage2 = "Please subscribe, or use a different world.";
                return;
            }
            if (k == 13) {
                loginMessage1 = "Could not complete login.";
                loginMessage2 = "Please try using a different world.";
                return;
            }
            if (k == 14) {
                loginMessage1 = "The server is being updated.";
                loginMessage2 = "Please wait 1 minute and try again.";
                return;
            }
            if (k == 15) {
                loggedIn = true;
                stream.pos = 0;
                inStream.pos = 0;
                pktType = -1;
                anInt841 = -1;
                anInt842 = -1;
                anInt843 = -1;
                pktSize = 0;
                anInt1009 = 0;
                anInt1104 = 0;
                menuActionRow = 0;
                menuOpen = false;
                aLong824 = System.currentTimeMillis();
                return;
            }
            if (k == 16) {
                loginMessage1 = "Login attempts exceeded.";
                loginMessage2 = "Please wait 1 minute and try again.";
                return;
            }
            if (k == 17) {
                loginMessage1 = "You are standing in a members-only area.";
                loginMessage2 = "To play on this world move to a free area first";
                return;
            }
            if (k == 20) {
                loginMessage1 = "Invalid loginserver requested";
                loginMessage2 = "Please try using a different world.";
                return;
            }
            if (k == 21) {
                for (int k1 = socketStream.read(); k1 >= 0; k1--) {
                    loginMessage1 = "You have only just left another world";
                    loginMessage2 = "Your profile will be transferred in: " + k1 + " seconds";
                    drawLoginScreen(true, 0);
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception _ex) {
                    }
                }

                login(s, s1, flag);
                return;
            }
            if (k == -1) {
                if (i1 == 0) {
                    if (loginFailures < 2) {
                        try {
                            Thread.sleep(2000L);
                        } catch (Exception _ex) {
                        }
                        loginFailures++;
                        login(s, s1, flag);
                        return;
                    } else {
                        loginMessage1 = "No response from loginserver";
                        loginMessage2 = "Please wait 1 minute and try again.";
                        return;
                    }
                } else {
                    loginMessage1 = "No response from server";
                    loginMessage2 = "Please try using a different world.";
                    return;
                }
            } else {
                System.out.println("response:" + k);
                loginMessage1 = "Unexpected server response";
                loginMessage2 = "Please try using a different world.";
                return;
            }
        } catch (IOException _ex) {
            loginMessage1 = "";
        }
        loginMessage2 = "Error connecting to server.";
    }

    private boolean doWalkTo(int i, int j, int k, int i1, int j1, int k1, int l1, int i2, int j2, boolean flag, int k2) {
        byte byte0 = 104;
        byte byte1 = 104;
        for (int l2 = 0; l2 < byte0; l2++) {
            for (int i3 = 0; i3 < byte1; i3++) {
                anIntArrayArray901[l2][i3] = 0;
                anIntArrayArray825[l2][i3] = 0x5f5e0ff;
            }

        }

        int j3 = j2;
        int k3 = j1;
        anIntArrayArray901[j2][j1] = 99;
        anIntArrayArray825[j2][j1] = 0;
        int l3 = 0;
        int i4 = 0;
        bigX[l3] = j2;
        bigY[l3++] = j1;
        boolean flag1 = false;
        int j4 = bigX.length;
        int ai[][] = tileSettings[plane].clipData;
        while (i4 != l3) {
            j3 = bigX[i4];
            k3 = bigY[i4];
            i4 = (i4 + 1) % j4;
            if (j3 == k2 && k3 == i2) {
                flag1 = true;
                break;
            }
            if (i1 != 0) {
                if ((i1 < 5 || i1 == 10) && tileSettings[plane].isWalkableA(k2, j3, k3, j, i1 - 1, i2)) {
                    flag1 = true;
                    break;
                }
                if (i1 < 10 && tileSettings[plane].isWalkableB(k2, i2, k3, i1 - 1, j, j3)) {
                    flag1 = true;
                    break;
                }
            }
            if (k1 != 0 && k != 0 && tileSettings[plane].isWalkableC(i2, k2, j3, k, l1, k1, k3)) {
                flag1 = true;
                break;
            }
            int l4 = anIntArrayArray825[j3][k3] + 1;
            if (j3 > 0 && anIntArrayArray901[j3 - 1][k3] == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0) {
                bigX[l3] = j3 - 1;
                bigY[l3] = k3;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3 - 1][k3] = 2;
                anIntArrayArray825[j3 - 1][k3] = l4;
            }
            if (j3 < byte0 - 1 && anIntArrayArray901[j3 + 1][k3] == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0) {
                bigX[l3] = j3 + 1;
                bigY[l3] = k3;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3 + 1][k3] = 8;
                anIntArrayArray825[j3 + 1][k3] = l4;
            }
            if (k3 > 0 && anIntArrayArray901[j3][k3 - 1] == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
                bigX[l3] = j3;
                bigY[l3] = k3 - 1;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3][k3 - 1] = 1;
                anIntArrayArray825[j3][k3 - 1] = l4;
            }
            if (k3 < byte1 - 1 && anIntArrayArray901[j3][k3 + 1] == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0) {
                bigX[l3] = j3;
                bigY[l3] = k3 + 1;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3][k3 + 1] = 4;
                anIntArrayArray825[j3][k3 + 1] = l4;
            }
            if (j3 > 0 && k3 > 0 && anIntArrayArray901[j3 - 1][k3 - 1] == 0 && (ai[j3 - 1][k3 - 1] & 0x128010e) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
                bigX[l3] = j3 - 1;
                bigY[l3] = k3 - 1;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3 - 1][k3 - 1] = 3;
                anIntArrayArray825[j3 - 1][k3 - 1] = l4;
            }
            if (j3 < byte0 - 1 && k3 > 0 && anIntArrayArray901[j3 + 1][k3 - 1] == 0 && (ai[j3 + 1][k3 - 1] & 0x1280183) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
                bigX[l3] = j3 + 1;
                bigY[l3] = k3 - 1;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3 + 1][k3 - 1] = 9;
                anIntArrayArray825[j3 + 1][k3 - 1] = l4;
            }
            if (j3 > 0 && k3 < byte1 - 1 && anIntArrayArray901[j3 - 1][k3 + 1] == 0 && (ai[j3 - 1][k3 + 1] & 0x1280138) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0) {
                bigX[l3] = j3 - 1;
                bigY[l3] = k3 + 1;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3 - 1][k3 + 1] = 6;
                anIntArrayArray825[j3 - 1][k3 + 1] = l4;
            }
            if (j3 < byte0 - 1 && k3 < byte1 - 1 && anIntArrayArray901[j3 + 1][k3 + 1] == 0 && (ai[j3 + 1][k3 + 1] & 0x12801e0) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0) {
                bigX[l3] = j3 + 1;
                bigY[l3] = k3 + 1;
                l3 = (l3 + 1) % j4;
                anIntArrayArray901[j3 + 1][k3 + 1] = 12;
                anIntArrayArray825[j3 + 1][k3 + 1] = l4;
            }
        }
        anInt1264 = 0;
        if (!flag1) {
            if (flag) {
                int i5 = 100;
                for (int k5 = 1; k5 < 2; k5++) {
                    for (int i6 = k2 - k5; i6 <= k2 + k5; i6++) {
                        for (int l6 = i2 - k5; l6 <= i2 + k5; l6++)
                            if (i6 >= 0 && l6 >= 0 && i6 < 104 && l6 < 104 && anIntArrayArray825[i6][l6] < i5) {
                                i5 = anIntArrayArray825[i6][l6];
                                j3 = i6;
                                k3 = l6;
                                anInt1264 = 1;
                                flag1 = true;
                            }

                    }

                    if (flag1)
                        break;
                }

            }
            if (!flag1)
                return false;
        }
        i4 = 0;
        bigX[i4] = j3;
        bigY[i4++] = k3;
        int l5;
        for (int j5 = l5 = anIntArrayArray901[j3][k3]; j3 != j2 || k3 != j1; j5 = anIntArrayArray901[j3][k3]) {
            if (j5 != l5) {
                l5 = j5;
                bigX[i4] = j3;
                bigY[i4++] = k3;
            }
            if ((j5 & 2) != 0)
                j3++;
            else if ((j5 & 8) != 0)
                j3--;
            if ((j5 & 1) != 0)
                k3++;
            else if ((j5 & 4) != 0)
                k3--;
        }
        //	if(cancelWalk) { return i4 > 0; }


        if (i4 > 0) {
            int k4 = i4;
            if (k4 > 25)
                k4 = 25;
            i4--;
            int k6 = bigX[i4];
            int i7 = bigY[i4];
            anInt1288 += k4;
            if (anInt1288 >= 92) {
                stream.p1isaac(36);
                stream.p4(0);
                anInt1288 = 0;
            }
            if (i == 0) {
                stream.p1isaac(164);
                stream.p1(k4 + k4 + 3);
            }
            if (i == 1) {
                stream.p1isaac(248);
                stream.p1(k4 + k4 + 3 + 14);
            }
            if (i == 2) {
                stream.p1isaac(98);
                stream.p1(k4 + k4 + 3);
            }
            stream.isp2(k6 + baseX);
            destX = bigX[0];
            destY = bigY[0];
            for (int j7 = 1; j7 < k4; j7++) {
                i4--;
                stream.p1(bigX[i4] - k6);
                stream.p1(bigY[i4] - i7);
            }

            stream.ip2(i7 + baseY);
            stream.np1(super.keyStatus[5] != 1 ? 0 : 1);
            return true;
        }
        return i != 1;
    }

    private void readNpcUpdateMask(Packet stream) {
        for (int j = 0; j < sessionNpcsAwaitingUpdatePtr; j++) {
            int k = session_npcs_awaiting_update[j];
            Npc npc = sessionNpcs[k];
            int l = stream.g1();
            if ((l & 0x10) != 0) {
                int i1 = stream.ig2();
                if (i1 == 65535)
                    i1 = -1;
                int i2 = stream.g1();
                if (i1 == npc.animation && i1 != -1) {
                    int l2 = Sequence.anims[i1].anInt365;
                    if (l2 == 1) {
                        npc.anInt1527 = 0;
                        npc.anInt1528 = 0;
                        npc.anInt1529 = i2;
                        npc.anInt1530 = 0;
                    }
                    if (l2 == 2)
                        npc.anInt1530 = 0;
                } else if (i1 == -1 || npc.animation == -1 || Sequence.anims[i1].anInt359 >= Sequence.anims[npc.animation].anInt359) {
                    npc.animation = i1;
                    npc.anInt1527 = 0;
                    npc.anInt1528 = 0;
                    npc.anInt1529 = i2;
                    npc.anInt1530 = 0;
                    npc.anInt1542 = npc.pathLength;
                }
            }
            if ((l & 8) != 0) {
                int j1 = stream.nsp1();
                int j2 = stream.ng1b();
                npc.updateHitData(j2, j1, currentTime);
                npc.loopCycleStatus = currentTime + 300;
                npc.currentHealth = stream.nsp1();
                npc.maxHealth = stream.g1();
            }
            if ((l & 0x80) != 0) {
                npc.anInt1520 = stream.g2();
                int k1 = stream.g4();
                npc.anInt1524 = k1 >> 16;
                npc.anInt1523 = currentTime + (k1 & 0xffff);
                npc.anInt1521 = 0;
                npc.anInt1522 = 0;
                if (npc.anInt1523 > currentTime)
                    npc.anInt1521 = -1;
                if (npc.anInt1520 == 65535)
                    npc.anInt1520 = -1;
            }
            if ((l & 0x20) != 0) {
                npc.interactingEntity = stream.g2();
                if (npc.interactingEntity == 65535)
                    npc.interactingEntity = -1;
            }
            if ((l & 1) != 0) {
                npc.textSpoken = stream.gstr();
                npc.textCycle = 100;
                //	entityMessage(npc);

            }
            if ((l & 0x40) != 0) {
                int l1 = stream.ng1b();
                int k2 = stream.sg1();
                npc.updateHitData(k2, l1, currentTime);
                npc.loopCycleStatus = currentTime + 300;
                npc.currentHealth = stream.sg1();
                npc.maxHealth = stream.ng1b();
            }
            if ((l & 2) != 0) {
                npc.desc = NpcDef.forID(stream.isg2());
                npc.boundDim = npc.desc.aByte68;
                npc.anInt1504 = npc.desc.anInt79;
                npc.anInt1554 = npc.desc.anInt67;
                npc.anInt1555 = npc.desc.anInt58;
                npc.anInt1556 = npc.desc.anInt83;
                npc.anInt1557 = npc.desc.anInt55;
                npc.anInt1511 = npc.desc.idleAnimation;
            }
            if ((l & 4) != 0) {
                npc.anInt1538 = stream.ig2();
                npc.anInt1539 = stream.ig2();
            }
        }
    }

    private void buildAtNPCMenu(NpcDef npcDef, int i, int j, int k) {
        if (menuActionRow >= 400)
            return;
        if (npcDef.childrenIDs != null)
            npcDef = npcDef.method161();
        if (npcDef == null)
            return;
        if (!npcDef.aBoolean84)
            return;
        String s = npcDef.name;
        if (npcDef.combatLevel != 0)
            s = s + logic_get_combat_risk_colour(session_player.combatLevel, npcDef.combatLevel) + " (level-" + npcDef.combatLevel + ")";
        if (itemSelected == 1) {
            menuActionName[menuActionRow] = "Use " + selectedItemName + " with @yel@" + s;
            menuActionID[menuActionRow] = 582;
            menuActionCmd1[menuActionRow] = i;
            menuActionCmd2[menuActionRow] = k;
            menuActionCmd3[menuActionRow] = j;
            menuActionRow++;
            return;
        }
        if (spellSelected == 1) {
            if ((spellUsableOn & 2) == 2) {
                menuActionName[menuActionRow] = spellTooltip + " @yel@" + s;
                menuActionID[menuActionRow] = 413;
                menuActionCmd1[menuActionRow] = i;
                menuActionCmd2[menuActionRow] = k;
                menuActionCmd3[menuActionRow] = j;
                menuActionRow++;
            }
        } else {
            if (npcDef.actions != null) {
                for (int l = 4; l >= 0; l--)
                    if (npcDef.actions[l] != null && !npcDef.actions[l].equalsIgnoreCase("attack")) {
                        menuActionName[menuActionRow] = npcDef.actions[l] + " @yel@" + s;
                        if (l == 0)
                            menuActionID[menuActionRow] = 20;
                        if (l == 1)
                            menuActionID[menuActionRow] = 412;
                        if (l == 2)
                            menuActionID[menuActionRow] = 225;
                        if (l == 3)
                            menuActionID[menuActionRow] = 965;
                        if (l == 4)
                            menuActionID[menuActionRow] = 478;
                        menuActionCmd1[menuActionRow] = i;
                        menuActionCmd2[menuActionRow] = k;
                        menuActionCmd3[menuActionRow] = j;
                        menuActionRow++;
                    }

            }
            if (npcDef.actions != null) {
                for (int i1 = 4; i1 >= 0; i1--)
                    if (npcDef.actions[i1] != null && npcDef.actions[i1].equalsIgnoreCase("attack")) {
                        char c = '\0';
                        if (npcDef.combatLevel > session_player.combatLevel)
                            c = '\u07D0';
                        menuActionName[menuActionRow] = npcDef.actions[i1] + " @yel@" + s;
                        if (i1 == 0)
                            menuActionID[menuActionRow] = 20 + c;
                        if (i1 == 1)
                            menuActionID[menuActionRow] = 412 + c;
                        if (i1 == 2)
                            menuActionID[menuActionRow] = 225 + c;
                        if (i1 == 3)
                            menuActionID[menuActionRow] = 965 + c;
                        if (i1 == 4)
                            menuActionID[menuActionRow] = 478 + c;
                        menuActionCmd1[menuActionRow] = i;
                        menuActionCmd2[menuActionRow] = k;
                        menuActionCmd3[menuActionRow] = j;
                        menuActionRow++;
                    }

            }
            menuActionName[menuActionRow] = "Examine @yel@" + s + " @gre@(@whi@" + npcDef.type + "@gre@)";
            menuActionID[menuActionRow] = 1025;
            menuActionCmd1[menuActionRow] = i;
            menuActionCmd2[menuActionRow] = k;
            menuActionCmd3[menuActionRow] = j;
            menuActionRow++;
        }
    }

    private void buildAtPlayerMenu(int i, int j, Player player, int k) {
        if (player == session_player)
            return;
        if (menuActionRow >= 400)
            return;
        String s;
        if (player.skill == 0)
            s = player.name + logic_get_combat_risk_colour(session_player.combatLevel, player.combatLevel) + " (level-" + player.combatLevel + ")";
        else
            s = player.name + " (skill-" + player.skill + ")";
        if (itemSelected == 1) {
            menuActionName[menuActionRow] = "Use " + selectedItemName + " with @whi@" + s;
            menuActionID[menuActionRow] = 491;
            menuActionCmd1[menuActionRow] = j;
            menuActionCmd2[menuActionRow] = i;
            menuActionCmd3[menuActionRow] = k;
            menuActionRow++;
        } else if (spellSelected == 1) {
            if ((spellUsableOn & 8) == 8) {
                menuActionName[menuActionRow] = spellTooltip + " @whi@" + s;
                menuActionID[menuActionRow] = 365;
                menuActionCmd1[menuActionRow] = j;
                menuActionCmd2[menuActionRow] = i;
                menuActionCmd3[menuActionRow] = k;
                menuActionRow++;
            }
        } else {
            for (int l = 4; l >= 0; l--)
                if (atPlayerActions[l] != null) {
                    menuActionName[menuActionRow] = atPlayerActions[l] + " @whi@" + s;
                    char c = '\0';
                    if (atPlayerActions[l].equalsIgnoreCase("attack")) {
                        if (player.combatLevel > session_player.combatLevel)
                            c = '\u07D0';
                        if (session_player.team != 0 && player.team != 0)
                            if (session_player.team == player.team)
                                c = '\u07D0';
                            else
                                c = '\0';
                    } else if (atPlayerArray[l])
                        c = '\u07D0';
                    if (l == 0)
                        menuActionID[menuActionRow] = 561 + c;
                    if (l == 1)
                        menuActionID[menuActionRow] = 779 + c;
                    if (l == 2)
                        menuActionID[menuActionRow] = 27 + c;
                    if (l == 3)
                        menuActionID[menuActionRow] = 577 + c;
                    if (l == 4)
                        menuActionID[menuActionRow] = 729 + c;
                    menuActionCmd1[menuActionRow] = j;
                    menuActionCmd2[menuActionRow] = i;
                    menuActionCmd3[menuActionRow] = k;
                    menuActionRow++;
                }

        }
        for (int i1 = 0; i1 < menuActionRow; i1++)
            if (menuActionID[i1] == 516) {
                menuActionName[i1] = "Walk here @whi@" + s;
                return;
            }

    }

    private void method89(GameObjectSpawnRequest gameObjectSpawnRequest) {
        int i = 0;
        int j = -1;
        int k = 0;
        int l = 0;
        if (gameObjectSpawnRequest.anInt1296 == 0)
            i = sceneGraph.getWallObjectUID(gameObjectSpawnRequest.plane, gameObjectSpawnRequest.x, gameObjectSpawnRequest.z);
        if (gameObjectSpawnRequest.anInt1296 == 1)
            i = sceneGraph.getWallDecorationUID(gameObjectSpawnRequest.plane, gameObjectSpawnRequest.x, gameObjectSpawnRequest.z);
        if (gameObjectSpawnRequest.anInt1296 == 2)
            i = sceneGraph.getInteractableObjectUID(gameObjectSpawnRequest.plane, gameObjectSpawnRequest.x, gameObjectSpawnRequest.z);
        if (gameObjectSpawnRequest.anInt1296 == 3)
            i = sceneGraph.getGroundDecorationUID(gameObjectSpawnRequest.plane, gameObjectSpawnRequest.x, gameObjectSpawnRequest.z);
        if (i != 0) {
            int i1 = sceneGraph.getIDTAGForXYZ(gameObjectSpawnRequest.plane, gameObjectSpawnRequest.x, gameObjectSpawnRequest.z, i);
            j = i >> 14 & 0x7fff;
            k = i1 & 0x1f;
            l = i1 >> 6;
        }
        gameObjectSpawnRequest.id = j;
        gameObjectSpawnRequest.type = k;
        gameObjectSpawnRequest.face = l;
    }

    private void method90()//todo - handlemusicevents
    {
        for (int i = 0; i < anInt1062; i++)
            if (anIntArray1250[i] <= 0) {
                boolean flag1 = false;
                try {
                    if (anIntArray1207[i] == anInt874 && anIntArray1241[i] == anInt1289) {
                        if (!replayWave())
                            flag1 = true;
                    } else {
                        Packet stream = Sound.generateWaveData(anIntArray1241[i], anIntArray1207[i]);
                        if (System.currentTimeMillis() + (long) (stream.pos / 22) > aLong1172 + (long) (anInt1257 / 22)) {
                            anInt1257 = stream.pos;
                            aLong1172 = System.currentTimeMillis();
                            if (saveWave(stream.data, stream.pos)) {
                                anInt874 = anIntArray1207[i];
                                anInt1289 = anIntArray1241[i];
                            } else {
                                flag1 = true;
                            }
                        }
                    }
                } catch (Exception exception) {
                }
                if (!flag1 || anIntArray1250[i] == -5) {
                    anInt1062--;
                    for (int j = i; j < anInt1062; j++) {
                        anIntArray1207[j] = anIntArray1207[j + 1];
                        anIntArray1241[j] = anIntArray1241[j + 1];
                        anIntArray1250[j] = anIntArray1250[j + 1];
                    }

                    i--;
                } else {
                    anIntArray1250[i] = -5;
                }
            } else {
                anIntArray1250[i]--;
            }

        if (prevSong > 0) {
            prevSong -= 20;
            if (prevSong < 0)
                prevSong = 0;
            if (prevSong == 0 && musicEnabled && !lowMem) {
                nextSong = currentSong;
                songChanging = true;
                onDemandFetcher.method558(2, nextSong);
            }
        }
    }

    protected void startUp() {
        drawLoadingText(20, "Starting up");
        if (Signlink.sunjava)
            super.minDelay = 5;
        if (aBoolean993) {
            //           rsAlreadyLoaded = true;
            //           return;
        }
        aBoolean993 = true;
        boolean flag = true;
        String s = getDocumentBaseHost();
        if (s.endsWith("jagex.com"))
            flag = true;
        if (s.endsWith("runescape.com"))
            flag = true;
        if (s.endsWith("192.168.1.2"))
            flag = true;
        if (s.endsWith("192.168.1.229"))
            flag = true;
        if (s.endsWith("192.168.1.228"))
            flag = true;
        if (s.endsWith("192.168.1.227"))
            flag = true;
        if (s.endsWith("192.168.1.226"))
            flag = true;
        if (s.endsWith("127.0.0.1"))
            flag = true;
        if (!flag) {
            genericLoadingError = true;
            return;
        }
        if (Signlink.cache_dat != null) {
            for (int i = 0; i < 5; i++)
                jagexFileStores[i] = new JagexFileStore(Signlink.cache_dat, Signlink.cache_idx[i], i + 1);

        }
        try {
            connectServer();
            titleJagexArchive = streamLoaderForName(1, "title screen", "title", expectedCRCs[1], 25);
            smallFont = new RSFont(false, "p11_full", titleJagexArchive);
            plainFont = new RSFont(false, "p12_full", titleJagexArchive);
            boldFont = new RSFont(false, "b12_full", titleJagexArchive);
            RSFont fancyFont = new RSFont(true, "q8_full", titleJagexArchive);
            drawLogo();
            loadTitleScreen();
            JagexArchive jagexArchive = streamLoaderForName(2, "config", "config", expectedCRCs[2], 30);
            JagexArchive jagexArchive_1 = streamLoaderForName(3, "interface", "interface", expectedCRCs[3], 35);
            JagexArchive jagexArchive_2 = streamLoaderForName(4, "2d graphics", "media", expectedCRCs[4], 40);
            JagexArchive jagexArchive_3 = streamLoaderForName(6, "textures", "textures", expectedCRCs[6], 45);
            JagexArchive jagexArchive_4 = streamLoaderForName(7, "chat system", "wordenc", expectedCRCs[7], 50);
            JagexArchive jagexArchive_5 = streamLoaderForName(8, "sound effects", "sounds", expectedCRCs[8], 55);
            tileSettingBits = new byte[4][104][104];
            intGroundArray = new int[4][105][105];
            sceneGraph = new SceneGraph(4, 104, 104, intGroundArray);
            for (int j = 0; j < 4; j++)
                tileSettings[j] = new TileSetting(104, 104);

            minimapImage = new RgbImage(512, 512);
            JagexArchive jagexArchive_6 = streamLoaderForName(5, "update list", "versionlist", expectedCRCs[5], 60);
            drawLoadingText(60, "Connecting to update server");
            drawLogo();
            onDemandFetcher = new OnDemandFetcher();
            onDemandFetcher.start(jagexArchive_6, this);
            Animation.initialize(onDemandFetcher.getAnimCount());
            Model.initialize(onDemandFetcher.getVersionCount(0), onDemandFetcher);
            //preloadModels();
            if (!lowMem) {
                nextSong = 0;
                try {
                    nextSong = Integer.parseInt(getParameter("music"));
                } catch (Exception _ex) {
                }
                songChanging = true;
                onDemandFetcher.method558(2, nextSong);
                while (onDemandFetcher.getNodeCount() > 0) {
                    on_demand_process();
                    try {
                        Thread.sleep(100L);
                    } catch (Exception _ex) {
                    }
                    if (onDemandFetcher.anInt1349 > 3) {
                        loadError();
                        return;
                    }
                }
            }
            drawLoadingText(65, "Requesting animations");
            int k = onDemandFetcher.getVersionCount(1);
            for (int i1 = 0; i1 < k; i1++)
                onDemandFetcher.method558(1, i1);

            while (onDemandFetcher.getNodeCount() > 0) {
                int j1 = k - onDemandFetcher.getNodeCount();
                if (j1 > 0)
                    drawLoadingText(65, "Loading animations - " + (j1 * 100) / k + "%");
                on_demand_process();
                try {
                    Thread.sleep(100L);
                } catch (Exception _ex) {
                }
                if (onDemandFetcher.anInt1349 > 3) {
                    loadError();
                    return;
                }
            }
            drawLoadingText(70, "Requesting models");
            k = onDemandFetcher.getVersionCount(0);
            for (int k1 = 0; k1 < k; k1++) {
                int l1 = onDemandFetcher.getModelIndex(k1);
                if ((l1 & 1) != 0)
                    onDemandFetcher.method558(0, k1);
            }

            k = onDemandFetcher.getNodeCount();
            while (onDemandFetcher.getNodeCount() > 0) {
                int i2 = k - onDemandFetcher.getNodeCount();
                if (i2 > 0)
                    drawLoadingText(70, "Loading models - " + (i2 * 100) / k + "%");
                on_demand_process();
                try {
                    Thread.sleep(100L);
                } catch (Exception _ex) {
                }
            }
            if (jagexFileStores[0] != null) {
                drawLoadingText(75, "Requesting maps");
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(0, 48, 47));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(1, 48, 47));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(0, 48, 48));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(1, 48, 48));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(0, 48, 49));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(1, 48, 49));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(0, 47, 47));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(1, 47, 47));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(0, 47, 48));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(1, 47, 48));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(0, 148, 48));
                onDemandFetcher.method558(3, onDemandFetcher.getMapIndex(1, 148, 48));
                k = onDemandFetcher.getNodeCount();
                while (onDemandFetcher.getNodeCount() > 0) {
                    int j2 = k - onDemandFetcher.getNodeCount();
                    if (j2 > 0)
                        drawLoadingText(75, "Loading maps - " + (j2 * 100) / k + "%");
                    on_demand_process();
                    try {
                        Thread.sleep(100L);
                    } catch (Exception _ex) {
                    }
                }
            }
            k = onDemandFetcher.getVersionCount(0);
            for (int k2 = 0; k2 < k; k2++) {
                int l2 = onDemandFetcher.getModelIndex(k2);
                byte byte0 = 0;
                if ((l2 & 8) != 0)
                    byte0 = 10;
                else if ((l2 & 0x20) != 0)
                    byte0 = 9;
                else if ((l2 & 0x10) != 0)
                    byte0 = 8;
                else if ((l2 & 0x40) != 0)
                    byte0 = 7;
                else if ((l2 & 0x80) != 0)
                    byte0 = 6;
                else if ((l2 & 2) != 0)
                    byte0 = 5;
                else if ((l2 & 4) != 0)
                    byte0 = 4;
                if ((l2 & 1) != 0)
                    byte0 = 3;
                if (byte0 != 0)
                    onDemandFetcher.method563(byte0, 0, k2);
            }

            onDemandFetcher.method554(isMembers);
            if (!lowMem) {
                int l = onDemandFetcher.getVersionCount(2);
                for (int i3 = 1; i3 < l; i3++)
                    if (onDemandFetcher.method569(i3))
                        onDemandFetcher.method563((byte) 1, 2, i3);

            }
            drawLoadingText(80, "Unpacking media");
            for (int count = 0; count < statusGlobes.length; count++)
                statusGlobes[count] = new RgbImage("./globes/globes_" + count + ".png");
            invBack = new IndexedImage(jagexArchive_2, "invback", 0);
            chatBack = new IndexedImage(jagexArchive_2, "chatback", 0);
            mapBack = new IndexedImage(jagexArchive_2, "mapback", 0);
            backBase1 = new IndexedImage(jagexArchive_2, "backbase1", 0);
            backBase2 = new IndexedImage(jagexArchive_2, "backbase2", 0);
            backHmid1 = new IndexedImage(jagexArchive_2, "backhmid1", 0);
            for (int j3 = 0; j3 < 13; j3++)
                sideIcons[j3] = new IndexedImage(jagexArchive_2, "sideicons", j3);

            compass = new RgbImage(jagexArchive_2, "compass", 0);
            mapEdge = new RgbImage(jagexArchive_2, "mapedge", 0);
            mapEdge.method345();
            try {
                for (int k3 = 0; k3 < 100; k3++)
                    mapScenes[k3] = new IndexedImage(jagexArchive_2, "mapscene", k3);

            } catch (Exception _ex) {
            }
            try {
                for (int l3 = 0; l3 < 100; l3++)
                    mapFunctions[l3] = new RgbImage(jagexArchive_2, "mapfunction", l3);

            } catch (Exception _ex) {
            }
            try {
                for (int i4 = 0; i4 < 20; i4++)
                    hitMarks[i4] = new RgbImage(jagexArchive_2, "hitmarks", i4);

            } catch (Exception _ex) {
            }
            try {
                for (int j4 = 0; j4 < 20; j4++)
                    headIcons[j4] = new RgbImage(jagexArchive_2, "headicons", j4);

            } catch (Exception _ex) {
            }
            mapFlag = new RgbImage(jagexArchive_2, "mapmarker", 0);
            mapMarker = new RgbImage(jagexArchive_2, "mapmarker", 1);
            for (int k4 = 0; k4 < 8; k4++)
                crosses[k4] = new RgbImage(jagexArchive_2, "cross", k4);

            mapDotItem = new RgbImage(jagexArchive_2, "mapdots", 0);
            mapDotNPC = new RgbImage(jagexArchive_2, "mapdots", 1);
            mapDotPlayer = new RgbImage(jagexArchive_2, "mapdots", 2);
            mapDotFriend = new RgbImage(jagexArchive_2, "mapdots", 3);
            mapDotTeam = new RgbImage(jagexArchive_2, "mapdots", 4);
            scrollBar1 = new IndexedImage(jagexArchive_2, "scrollbar", 0);
            scrollBar2 = new IndexedImage(jagexArchive_2, "scrollbar", 1);
            redStone1 = new IndexedImage(jagexArchive_2, "redstone1", 0);
            redStone2 = new IndexedImage(jagexArchive_2, "redstone2", 0);
            redStone3 = new IndexedImage(jagexArchive_2, "redstone3", 0);
            redStone1_2 = new IndexedImage(jagexArchive_2, "redstone1", 0);
            redStone1_2.flipHorizontal();
            redStone2_2 = new IndexedImage(jagexArchive_2, "redstone2", 0);
            redStone2_2.flipHorizontal();
            redStone1_3 = new IndexedImage(jagexArchive_2, "redstone1", 0);
            redStone1_3.flipVertical();
            redStone2_3 = new IndexedImage(jagexArchive_2, "redstone2", 0);
            redStone2_3.flipVertical();
            redStone3_2 = new IndexedImage(jagexArchive_2, "redstone3", 0);
            redStone3_2.flipVertical();
            redStone1_4 = new IndexedImage(jagexArchive_2, "redstone1", 0);
            redStone1_4.flipHorizontal();
            redStone1_4.flipVertical();
            redStone2_4 = new IndexedImage(jagexArchive_2, "redstone2", 0);
            redStone2_4.flipHorizontal();
            redStone2_4.flipVertical();
            for (int l4 = 0; l4 < 2; l4++)
                modIcons[l4] = new IndexedImage(jagexArchive_2, "mod_icons", l4);

            RgbImage rgbImage = new RgbImage(jagexArchive_2, "backleft1", 0);
            backLeftIP1 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backleft2", 0);
            backLeftIP2 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backright1", 0);
            backRightIP1 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backright2", 0);
            backRightIP2 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backtop1", 0);
            backTopIP1 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backvmid1", 0);
            backVmidIP1 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backvmid2", 0);
            backVmidIP2 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backvmid3", 0);
            backVmidIP3 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            rgbImage = new RgbImage(jagexArchive_2, "backhmid2", 0);
            backVmidIP2_2 = new GraphicsBuffer(rgbImage.myWidth, rgbImage.myHeight, getGameComponent());
            rgbImage.method346(0, 0);
            int i5 = (int) (Math.random() * 21D) - 10;
            int j5 = (int) (Math.random() * 21D) - 10;
            int k5 = (int) (Math.random() * 21D) - 10;
            int l5 = (int) (Math.random() * 41D) - 20;
            for (int i6 = 0; i6 < 100; i6++) {
                if (mapFunctions[i6] != null)
                    mapFunctions[i6].shiftColours(i5 + l5, j5 + l5, k5 + l5);
                if (mapScenes[i6] != null)
                    mapScenes[i6].shiftColours(i5 + l5, j5 + l5, k5 + l5);
            }

            drawLoadingText(83, "Unpacking textures");
            Rasterizer.unpackTextures(jagexArchive_3);
            Rasterizer.calculatePalette(0.80000000000000004D);
            Rasterizer.resetTextures(20);
            drawLoadingText(86, "Unpacking config");
            Sequence.unpackConfig(jagexArchive);
            ObjectDef.unpackConfig(jagexArchive);
            Floor.unpackConfig(jagexArchive);
            ItemDef.unpackConfig(jagexArchive);
            NpcDef.unpackConfig(jagexArchive);
            IdentityKit.unpackConfig(jagexArchive);
            SpotAnim.unpackConfig(jagexArchive);
            SettingUsagePointers.unpackConfig(jagexArchive);
            VarBit.unpackConfig(jagexArchive);
            ItemDef.isMembers = isMembers;
            if (!lowMem) {
                drawLoadingText(90, "Unpacking sounds");
                byte abyte0[] = jagexArchive_5.getDataForName("sounds.dat");
                Packet stream = new Packet(abyte0);
                Sound.unpack(stream);
            }
            drawLoadingText(95, "Unpacking interfaces");
            RSFont aclass30_sub2_sub1_sub4s[] = {smallFont, plainFont, boldFont, fancyFont};
            RSInterface.unpack(jagexArchive_1, aclass30_sub2_sub1_sub4s, jagexArchive_2);
            drawLoadingText(100, "Preparing game engine");
            drawLogo();
            for (int _y = 0; _y < 33; _y++) {
                int firstXOfLine = 999;
                int lastXOfLine = 0;
                for (int _x = 0; _x < 34; _x++) {
                    if (mapBack.imgPixels[_x + _y * mapBack.imgWidth] == 0) {
                        if (firstXOfLine == 999)
                            firstXOfLine = _x;
                        continue;
                    }//IF we are here pixel is not part of compass
                    if (firstXOfLine == 999)
                        continue;
                    lastXOfLine = _x;
                    break;
                }

                compassShape1[_y] = firstXOfLine;
                compassShape2[_y] = lastXOfLine - firstXOfLine;
            }

            for (int l6 = 5; l6 < 156; l6++) {
                int j7 = 999;
                int l7 = 0;
                for (int j8 = 25; j8 < 172; j8++) {
                    if (mapBack.imgPixels[j8 + l6 * mapBack.imgWidth] == 0 && (j8 > 34 || l6 > 34)) {
                        if (j7 == 999)
                            j7 = j8;
                        continue;
                    }
                    if (j7 == 999)
                        continue;
                    l7 = j8;
                    break;
                }

                minimapShape1[l6 - 5] = j7 - 25;
                minimapShape2[l6 - 5] = l7 - j7;
            }

            Rasterizer.setBounds(479, 96);
            anIntArray1180 = Rasterizer.lineOffsets;
            Rasterizer.setBounds(190, 261);
            anIntArray1181 = Rasterizer.lineOffsets;
            Rasterizer.setBounds(512, 334);
            anIntArray1182 = Rasterizer.lineOffsets;
            int ai[] = new int[64];
            for (int i8 = 0; i8 < 64; i8++) {
                int k8 = i8 * 32 + 15;
                int l8 = 600 + k8 * 3;
                int i9 = Rasterizer.SINE[k8];
                ai[i8] = l8 * i9 >> 16;
            }

            SceneGraph.setupViewport(500, 800, 512, 334, ai);
            Censor.loadConfig(jagexArchive_4);
            mouseDetection = new MouseDetection(this);
            startRunnable(mouseDetection, 10);
            ObjectOnTile.clientInstance = this;
            SceneGraph.clientInstance = this;
            ObjectDef.clientInstance = this;
            NpcDef.clientInstance = this;
         //   pglWrapper.initJgle();
           // EditorMain.main(null);
            return;
        } catch (Exception exception) {
            Signlink.reporterror("loaderror " + aString1049 + " " + anInt1079);
            exception.printStackTrace();
        }
        loadingError = true;
    }

    private void updateOtherPlayerMovements(Packet stream, int i) {
        while (stream.bit_pos + 10 < i * 8) {
            int j = stream.gbits(11);
            if (j == 2047)
                break;
            if (session_players[j] == null) {
                session_players[j] = new Player();
                if (aStreamArray895s[j] != null)
                    session_players[j].updatePlayer(aStreamArray895s[j]);
            }
            session_player_list[session_player_count++] = j;
            Player player = session_players[j];
            player.time = currentTime;
            int k = stream.gbits(1);
            if (k == 1)
                session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = j;
            int l = stream.gbits(1);
            int i1 = stream.gbits(5);
            if (i1 > 15)
                i1 -= 32;
            int j1 = stream.gbits(5);
            if (j1 > 15)
                j1 -= 32;
            player.setPos(session_player.pathX[0] + j1, session_player.pathY[0] + i1, l == 1);
        }
        stream.end_bit_block();
    }

    private void processMainScreenClick()//minimap walking
    {
        if (miniMapLock != 0)
            return;
        if (super.mouseButtonPressed == 1) {
            int i = super.clickX - 25 - 550;
            int j = super.clickY - 5 - 4;
            if (i >= 0 && j >= 0 && i < 146 && j < 151) {
                i -= 73;
                j -= 75;
                int k = minimapInt1 + minimapInt2 & 0x7ff;
                int i1 = Rasterizer.SINE[k];
                int j1 = Rasterizer.COSINE[k];
                i1 = i1 * (minimapInt3 + 256) >> 8;
                j1 = j1 * (minimapInt3 + 256) >> 8;
                int k1 = j * i1 + i * j1 >> 11;
                int l1 = j * j1 - i * i1 >> 11;
                int i2 = session_player.boundExtentX + k1 >> 7;
                int j2 = session_player.boundExtentY - l1 >> 7;
                boolean flag1 = doWalkTo(1, 0, 0, 0, session_player.pathY[0], 0, 0, j2, session_player.pathX[0], true, i2);
                if (flag1) {
                    stream.p1(i);
                    stream.p1(j);
                    stream.p2(minimapInt1);
                    stream.p1(57);
                    stream.p1(minimapInt2);
                    stream.p1(minimapInt3);
                    stream.p1(89);
                    stream.p2(session_player.boundExtentX);
                    stream.p2(session_player.boundExtentY);
                    stream.p1(anInt1264);
                    stream.p1(63);
                }
            }
            anInt1117++;
            if (anInt1117 > 1151) {
                anInt1117 = 0;
                stream.p1isaac(246);
                stream.p1(0);
                int l = stream.pos;
                if ((int) (Math.random() * 2D) == 0)
                    stream.p1(101);
                stream.p1(197);
                stream.p2((int) (Math.random() * 65536D));
                stream.p1((int) (Math.random() * 256D));
                stream.p1(67);
                stream.p2(14214);
                if ((int) (Math.random() * 2D) == 0)
                    stream.p2(29487);
                stream.p2((int) (Math.random() * 65536D));
                if ((int) (Math.random() * 2D) == 0)
                    stream.p1(220);
                stream.p1(180);
                stream.psize1(stream.pos - l);
            }
        }
    }

    private String interfaceIntToString(int j) {
        if (j < 0x3b9ac9ff)
            return String.valueOf(j);
        else
            return "*";
    }

    private void showErrorScreen() {
        Graphics g = getGameComponent().getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, 765, 503);
        setTargetFramerate(1);
        if (loadingError) {
            aBoolean831 = false;
            g.setFont(new Font("Helvetica", 1, 16));
            g.setColor(Color.yellow);
            int k = 35;
            g.drawString("Sorry, an error has occured whilst loading RuneScape", 30, k);
            k += 50;
            g.setColor(Color.white);
            g.drawString("To fix this try the following (in order):", 30, k);
            k += 50;
            g.setColor(Color.white);
            g.setFont(new Font("Helvetica", 1, 12));
            g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, k);
            k += 30;
            g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, k);
            k += 30;
            g.drawString("3: Try using a different game-world", 30, k);
            k += 30;
            g.drawString("4: Try rebooting your computer", 30, k);
            k += 30;
            g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, k);
        }
        if (genericLoadingError) {
            aBoolean831 = false;
            g.setFont(new Font("Helvetica", 1, 20));
            g.setColor(Color.white);
            g.drawString("Error - unable to load game!", 50, 50);
            g.drawString("To play RuneScape make sure you play from", 50, 100);
            g.drawString("http://www.runescape.com", 50, 150);
        }
        if (rsAlreadyLoaded) {
            aBoolean831 = false;
            g.setColor(Color.yellow);
            int l = 35;
            g.drawString("Error a copy of RuneScape already appears to be loaded", 30, l);
            l += 50;
            g.setColor(Color.white);
            g.drawString("To fix this try the following (in order):", 30, l);
            l += 50;
            g.setColor(Color.white);
            g.setFont(new Font("Helvetica", 1, 12));
            g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, l);
            l += 30;
            g.drawString("2: Try rebooting your computer, and reloading", 30, l);
            l += 30;
        }
    }

    public URL getCodeBase() {
        //if(Signlink.mainapp != null)
        //    return Signlink.mainapp.getCodeBase();
        try {
            if (super.gameFrame != null)
                return new URL(server + ":80");
        } catch (Exception _ex) {
        }
        return super.getCodeBase();
    }

    private void forceNpcUpdateBlock() {
        for (int j = 0; j < sessionNpcCount; j++) {
            int k = sessionNpcList[j];
            Npc npc = sessionNpcs[k];
            if (npc != null)
                entityUpdateBlock(npc);
        }
    }

    private void entityUpdateBlock(Mobile mobile)//realy?
    {
        if (mobile.boundExtentX < 128 || mobile.boundExtentY < 128 || mobile.boundExtentX >= 13184 || mobile.boundExtentY >= 13184) {
            mobile.animation = -1;
            mobile.anInt1520 = -1;
            mobile.anInt1547 = 0;
            mobile.anInt1548 = 0;
            mobile.boundExtentX = mobile.pathX[0] * 128 + mobile.boundDim * 64;
            mobile.boundExtentY = mobile.pathY[0] * 128 + mobile.boundDim * 64;
            mobile.method446();
        }
        if (mobile == session_player && (mobile.boundExtentX < 1536 || mobile.boundExtentY < 1536 || mobile.boundExtentX >= 11776 || mobile.boundExtentY >= 11776)) {
            mobile.animation = -1;
            mobile.anInt1520 = -1;
            mobile.anInt1547 = 0;
            mobile.anInt1548 = 0;
            mobile.boundExtentX = mobile.pathX[0] * 128 + mobile.boundDim * 64;
            mobile.boundExtentY = mobile.pathY[0] * 128 + mobile.boundDim * 64;
            mobile.method446();
        }
        if (mobile.anInt1547 > currentTime)
            refreshEntityPosition(mobile);
        else if (mobile.anInt1548 >= currentTime)
            refreshEntityFaceDirection(mobile);
        else
            method99(mobile);
        appendFocusDestination(mobile);
        appendAnimation(mobile);
    }

    private void refreshEntityPosition(Mobile mobile) {
        int i = mobile.anInt1547 - currentTime;
        int j = mobile.anInt1543 * 128 + mobile.boundDim * 64;
        int k = mobile.anInt1545 * 128 + mobile.boundDim * 64;
        mobile.boundExtentX += (j - mobile.boundExtentX) / i;
        mobile.boundExtentY += (k - mobile.boundExtentY) / i;
        mobile.anInt1503 = 0;
        if (mobile.anInt1549 == 0)
            mobile.turnDirection = 1024;
        if (mobile.anInt1549 == 1)
            mobile.turnDirection = 1536;
        if (mobile.anInt1549 == 2)
            mobile.turnDirection = 0;
        if (mobile.anInt1549 == 3)
            mobile.turnDirection = 512;
    }

    private void refreshEntityFaceDirection(Mobile mobile) {
        if (mobile.anInt1548 == currentTime || mobile.animation == -1 || mobile.anInt1529 != 0 || mobile.anInt1528 + 1 > Sequence.anims[mobile.animation].getFrameLength(mobile.anInt1527)) {
            int i = mobile.anInt1548 - mobile.anInt1547;
            int j = currentTime - mobile.anInt1547;
            int k = mobile.anInt1543 * 128 + mobile.boundDim * 64;
            int l = mobile.anInt1545 * 128 + mobile.boundDim * 64;
            int i1 = mobile.anInt1544 * 128 + mobile.boundDim * 64;
            int j1 = mobile.anInt1546 * 128 + mobile.boundDim * 64;
            mobile.boundExtentX = (k * (i - j) + i1 * j) / i;
            mobile.boundExtentY = (l * (i - j) + j1 * j) / i;
        }
        mobile.anInt1503 = 0;
        if (mobile.anInt1549 == 0)
            mobile.turnDirection = 1024;
        if (mobile.anInt1549 == 1)
            mobile.turnDirection = 1536;
        if (mobile.anInt1549 == 2)
            mobile.turnDirection = 0;
        if (mobile.anInt1549 == 3)
            mobile.turnDirection = 512;
        mobile.currentRotation = mobile.turnDirection;
    }

    private void method99(Mobile mobile) {
        mobile.anInt1517 = mobile.anInt1511;
        if (mobile.pathLength == 0) {
            mobile.anInt1503 = 0;
            return;
        }
        if (mobile.animation != -1 && mobile.anInt1529 == 0) {
            Sequence sequence = Sequence.anims[mobile.animation];
            if (mobile.anInt1542 > 0 && sequence.anInt363 == 0) {
                mobile.anInt1503++;
                return;
            }
            if (mobile.anInt1542 <= 0 && sequence.priority == 0) {
                mobile.anInt1503++;
                return;
            }
        }
        int i = mobile.boundExtentX;
        int j = mobile.boundExtentY;
        int k = mobile.pathX[mobile.pathLength - 1] * 128 + mobile.boundDim * 64;
        int l = mobile.pathY[mobile.pathLength - 1] * 128 + mobile.boundDim * 64;
        if (k - i > 256 || k - i < -256 || l - j > 256 || l - j < -256) {
            mobile.boundExtentX = k;
            mobile.boundExtentY = l;
            return;
        }
        if (i < k) {
            if (j < l)
                mobile.turnDirection = 1280;
            else if (j > l)
                mobile.turnDirection = 1792;
            else
                mobile.turnDirection = 1536;
        } else if (i > k) {
            if (j < l)
                mobile.turnDirection = 768;
            else if (j > l)
                mobile.turnDirection = 256;
            else
                mobile.turnDirection = 512;
        } else if (j < l)
            mobile.turnDirection = 1024;
        else
            mobile.turnDirection = 0;
        int i1 = mobile.turnDirection - mobile.currentRotation & 0x7ff;
        if (i1 > 1024)
            i1 -= 2048;
        int j1 = mobile.anInt1555;
        if (i1 >= -256 && i1 <= 256)
            j1 = mobile.anInt1554;
        else if (i1 >= 256 && i1 < 768)
            j1 = mobile.anInt1557;
        else if (i1 >= -768 && i1 <= -256)
            j1 = mobile.anInt1556;
        if (j1 == -1)
            j1 = mobile.anInt1554;
        mobile.anInt1517 = j1;
        int k1 = 4;
        if (mobile.currentRotation != mobile.turnDirection && mobile.interactingEntity == -1 && mobile.anInt1504 != 0)
            k1 = 2;
        if (mobile.pathLength > 2)
            k1 = 6;
        if (mobile.pathLength > 3)
            k1 = 8;
        if (mobile.anInt1503 > 0 && mobile.pathLength > 1) {
            k1 = 8;
            mobile.anInt1503--;
        }
        if (mobile.pathRun[mobile.pathLength - 1])
            k1 <<= 1;
        if (k1 >= 8 && mobile.anInt1517 == mobile.anInt1554 && mobile.anInt1505 != -1)
            mobile.anInt1517 = mobile.anInt1505;
        if (i < k) {
            mobile.boundExtentX += k1;
            if (mobile.boundExtentX > k)
                mobile.boundExtentX = k;
        } else if (i > k) {
            mobile.boundExtentX -= k1;
            if (mobile.boundExtentX < k)
                mobile.boundExtentX = k;
        }
        if (j < l) {
            mobile.boundExtentY += k1;
            if (mobile.boundExtentY > l)
                mobile.boundExtentY = l;
        } else if (j > l) {
            mobile.boundExtentY -= k1;
            if (mobile.boundExtentY < l)
                mobile.boundExtentY = l;
        }
        if (mobile.boundExtentX == k && mobile.boundExtentY == l) {
            mobile.pathLength--;
            if (mobile.anInt1542 > 0)
                mobile.anInt1542--;
        }
    }

    private void appendFocusDestination(Mobile mobile) {
        if (mobile.anInt1504 == 0)
            return;
        if (mobile.interactingEntity != -1 && mobile.interactingEntity < 32768) {
            Npc npc = sessionNpcs[mobile.interactingEntity];
            if (npc != null) {
                int i1 = mobile.boundExtentX - npc.boundExtentX;
                int k1 = mobile.boundExtentY - npc.boundExtentY;
                if (i1 != 0 || k1 != 0)
                    mobile.turnDirection = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
            }
        }
        if (mobile.interactingEntity >= 32768) {
            int j = mobile.interactingEntity - 32768;
            if (j == unknownInt10)
                j = session_player_idx;
            Player player = session_players[j];
            if (player != null) {
                int l1 = mobile.boundExtentX - player.boundExtentX;
                int i2 = mobile.boundExtentY - player.boundExtentY;
                if (l1 != 0 || i2 != 0)
                    mobile.turnDirection = (int) (Math.atan2(l1, i2) * 325.94900000000001D) & 0x7ff;
            }
        }
        if ((mobile.anInt1538 != 0 || mobile.anInt1539 != 0) && (mobile.pathLength == 0 || mobile.anInt1503 > 0)) {
            int k = mobile.boundExtentX - (mobile.anInt1538 - baseX - baseX) * 64;
            int j1 = mobile.boundExtentY - (mobile.anInt1539 - baseY - baseY) * 64;
            if (k != 0 || j1 != 0)
                mobile.turnDirection = (int) (Math.atan2(k, j1) * 325.94900000000001D) & 0x7ff;
            mobile.anInt1538 = 0;
            mobile.anInt1539 = 0;
        }
        int l = mobile.turnDirection - mobile.currentRotation & 0x7ff;
        if (l != 0) {
            if (l < mobile.anInt1504 || l > 2048 - mobile.anInt1504)
                mobile.currentRotation = mobile.turnDirection;
            else if (l > 1024)
                mobile.currentRotation -= mobile.anInt1504;
            else
                mobile.currentRotation += mobile.anInt1504;
            mobile.currentRotation &= 0x7ff;
            if (mobile.anInt1517 == mobile.anInt1511 && mobile.currentRotation != mobile.turnDirection) {
                if (mobile.anInt1512 != -1) {
                    mobile.anInt1517 = mobile.anInt1512;
                    return;
                }
                mobile.anInt1517 = mobile.anInt1554;
            }
        }
    }

    private void appendAnimation(Mobile mobile) {
        mobile.aBoolean1541 = false;
        if (mobile.anInt1517 != -1) {
            Sequence sequence = Sequence.anims[mobile.anInt1517];
            mobile.anInt1519++;
            if (mobile.anInt1518 < sequence.frameCount && mobile.anInt1519 > sequence.getFrameLength(mobile.anInt1518)) {
                mobile.anInt1519 = 0;
                mobile.anInt1518++;
            }
            if (mobile.anInt1518 >= sequence.frameCount) {
                mobile.anInt1519 = 0;
                mobile.anInt1518 = 0;
            }
        }
        if (mobile.anInt1520 != -1 && currentTime >= mobile.anInt1523) {
            if (mobile.anInt1521 < 0)
                mobile.anInt1521 = 0;
            Sequence sequence_1 = SpotAnim.cache[mobile.anInt1520].aSequence_407;
            for (mobile.anInt1522++; mobile.anInt1521 < sequence_1.frameCount && mobile.anInt1522 > sequence_1.getFrameLength(mobile.anInt1521); mobile.anInt1521++)
                mobile.anInt1522 -= sequence_1.getFrameLength(mobile.anInt1521);

            if (mobile.anInt1521 >= sequence_1.frameCount && (mobile.anInt1521 < 0 || mobile.anInt1521 >= sequence_1.frameCount))
                mobile.anInt1520 = -1;
        }
        if (mobile.animation != -1 && mobile.anInt1529 <= 1) {
            Sequence sequence_2 = Sequence.anims[mobile.animation];
            if (sequence_2.anInt363 == 1 && mobile.anInt1542 > 0 && mobile.anInt1547 <= currentTime && mobile.anInt1548 < currentTime) {
                mobile.anInt1529 = 1;
                return;
            }
        }
        if (mobile.animation != -1 && mobile.anInt1529 == 0) {
            Sequence sequence_3 = Sequence.anims[mobile.animation];
            for (mobile.anInt1528++; mobile.anInt1527 < sequence_3.frameCount && mobile.anInt1528 > sequence_3.getFrameLength(mobile.anInt1527); mobile.anInt1527++)
                mobile.anInt1528 -= sequence_3.getFrameLength(mobile.anInt1527);

            if (mobile.anInt1527 >= sequence_3.frameCount) {
                mobile.anInt1527 -= sequence_3.frameStep;
                mobile.anInt1530++;
                if (mobile.anInt1530 >= sequence_3.anInt362)
                    mobile.animation = -1;
                if (mobile.anInt1527 < 0 || mobile.anInt1527 >= sequence_3.frameCount)
                    mobile.animation = -1;
            }
            mobile.aBoolean1541 = sequence_3.aBoolean358;
        }
        if (mobile.anInt1529 > 0)
            mobile.anInt1529--;
    }

    private void drawGameScreen() {
        if (repaintRequested) {
            repaintRequested = false;
            backLeftIP1.drawGraphics(4, super.graphics, 0);
            backLeftIP2.drawGraphics(357, super.graphics, 0);
            backRightIP1.drawGraphics(4, super.graphics, 722);
            backRightIP2.drawGraphics(205, super.graphics, 743);
            backTopIP1.drawGraphics(0, super.graphics, 0);
            backVmidIP1.drawGraphics(4, super.graphics, 516);
            backVmidIP2.drawGraphics(205, super.graphics, 516);
            backVmidIP3.drawGraphics(357, super.graphics, 496);
            backVmidIP2_2.drawGraphics(338, super.graphics, 0);
            needDrawTabArea = true;
            inputTaken = true;
            tabAreaAltered = true;
            aBoolean1233 = true;
            if (loadingStage != 2) {
                gameScreenCanvas.drawGraphics(4, super.graphics, 4);
                minimapIP.drawGraphics(4, super.graphics, 550);
            }
        }
        if (loadingStage == 2)
            method146();
        if (menuOpen && menuScreenArea == 1)
            needDrawTabArea = true;
        if (invOverlayInterfaceID != -1) {
            boolean flag1 = animateRSInterface(anInt945, invOverlayInterfaceID);
            if (flag1)
                needDrawTabArea = true;
        }
        if (atInventoryInterfaceType == 2)
            needDrawTabArea = true;
        if (activeInterfaceType == 2)
            needDrawTabArea = true;
        if (needDrawTabArea) {
            drawTabArea();
            needDrawTabArea = false;
        }
        if (backDialogID == -1) {
            aClass9_1059.scrollPosition = anInt1211 - anInt1089 - 77;
            if (super.mouseEventX > 448 && super.mouseEventX < 560 && super.mouseEventY > 332)
                method65(463, 77, super.mouseEventX - 17, super.mouseEventY - 357, aClass9_1059, 0, false, anInt1211);
            int i = anInt1211 - 77 - aClass9_1059.scrollPosition;
            if (i < 0)
                i = 0;
            if (i > anInt1211 - 77)
                i = anInt1211 - 77;
            if (anInt1089 != i) {
                anInt1089 = i;
                inputTaken = true;
            }
        }
        if (backDialogID != -1) {
            boolean flag2 = animateRSInterface(anInt945, backDialogID);
            if (flag2)
                inputTaken = true;
        }
        if (atInventoryInterfaceType == 3)
            inputTaken = true;
        if (activeInterfaceType == 3)
            inputTaken = true;
        if (aString844 != null)
            inputTaken = true;
        if (menuOpen && menuScreenArea == 2)
            inputTaken = true;
        if (inputTaken) {
            drawChatArea();
            inputTaken = false;
        }
        if (loadingStage == 2) {
            drawMinimap();
            minimapIP.drawGraphics(4, super.graphics, 550);
        }
        if (anInt1054 != -1)
            tabAreaAltered = true;
        if (tabAreaAltered) {
            if (anInt1054 != -1 && anInt1054 == tabID) {
                anInt1054 = -1;
                stream.p1isaac(120);
                stream.p1(tabID);
            }
            tabAreaAltered = false;
            aRSImageProducer_1125.initDrawingArea();
            backHmid1.drawImage(0, 0);
            if (invOverlayInterfaceID == -1) {
                if (tabInterfaceIDs[tabID] != -1) {
                    if (tabID == 0)
                        redStone1.drawImage(22, 10);
                    if (tabID == 1)
                        redStone2.drawImage(54, 8);
                    if (tabID == 2)
                        redStone2.drawImage(82, 8);
                    if (tabID == 3)
                        redStone3.drawImage(110, 8);
                    if (tabID == 4)
                        redStone2_2.drawImage(153, 8);
                    if (tabID == 5)
                        redStone2_2.drawImage(181, 8);
                    if (tabID == 6)
                        redStone1_2.drawImage(209, 9);
                }
                if (tabInterfaceIDs[0] != -1 && (anInt1054 != 0 || currentTime % 20 < 10))
                    sideIcons[0].drawImage(29, 13);
                if (tabInterfaceIDs[1] != -1 && (anInt1054 != 1 || currentTime % 20 < 10))
                    sideIcons[1].drawImage(53, 11);
                if (tabInterfaceIDs[2] != -1 && (anInt1054 != 2 || currentTime % 20 < 10))
                    sideIcons[2].drawImage(82, 11);
                if (tabInterfaceIDs[3] != -1 && (anInt1054 != 3 || currentTime % 20 < 10))
                    sideIcons[3].drawImage(115, 12);
                if (tabInterfaceIDs[4] != -1 && (anInt1054 != 4 || currentTime % 20 < 10))
                    sideIcons[4].drawImage(153, 13);
                if (tabInterfaceIDs[5] != -1 && (anInt1054 != 5 || currentTime % 20 < 10))
                    sideIcons[5].drawImage(180, 11);
                if (tabInterfaceIDs[6] != -1 && (anInt1054 != 6 || currentTime % 20 < 10))
                    sideIcons[6].drawImage(208, 13);
            }
            aRSImageProducer_1125.drawGraphics(160, super.graphics, 516);
            aRSImageProducer_1124.initDrawingArea();
            backBase2.drawImage(0, 0);
            if (invOverlayInterfaceID == -1) {
                if (tabInterfaceIDs[tabID] != -1) {
                    if (tabID == 7)
                        redStone1_3.drawImage(42, 0);
                    if (tabID == 8)
                        redStone2_3.drawImage(74, 0);
                    if (tabID == 9)
                        redStone2_3.drawImage(102, 0);
                    if (tabID == 10)
                        redStone3_2.drawImage(130, 1);
                    if (tabID == 11)
                        redStone2_4.drawImage(173, 0);
                    if (tabID == 12)
                        redStone2_4.drawImage(201, 0);
                    if (tabID == 13)
                        redStone1_4.drawImage(229, 0);
                }
                if (tabInterfaceIDs[8] != -1 && (anInt1054 != 8 || currentTime % 20 < 10))
                    sideIcons[7].drawImage(74, 2);
                if (tabInterfaceIDs[9] != -1 && (anInt1054 != 9 || currentTime % 20 < 10))
                    sideIcons[8].drawImage(102, 3);
                if (tabInterfaceIDs[10] != -1 && (anInt1054 != 10 || currentTime % 20 < 10))
                    sideIcons[9].drawImage(137, 4);
                if (tabInterfaceIDs[11] != -1 && (anInt1054 != 11 || currentTime % 20 < 10))
                    sideIcons[10].drawImage(174, 2);
                if (tabInterfaceIDs[12] != -1 && (anInt1054 != 12 || currentTime % 20 < 10))
                    sideIcons[11].drawImage(201, 2);
                if (tabInterfaceIDs[13] != -1 && (anInt1054 != 13 || currentTime % 20 < 10))
                    sideIcons[12].drawImage(226, 2);
            }
            aRSImageProducer_1124.drawGraphics(466, super.graphics, 496);
            gameScreenCanvas.initDrawingArea();
        }
        if (aBoolean1233) {
            aBoolean1233 = false;
            aRSImageProducer_1123.initDrawingArea();
            backBase1.drawImage(0, 0);
            plainFont.method382(0xffffff, 55, "Public chat", 28, true);
            if (publicChatMode == 0)
                plainFont.method382(65280, 55, "On", 41, true);
            if (publicChatMode == 1)
                plainFont.method382(0xffff00, 55, "Friends", 41, true);
            if (publicChatMode == 2)
                plainFont.method382(0xff0000, 55, "Off", 41, true);
            if (publicChatMode == 3)
                plainFont.method382(65535, 55, "Hide", 41, true);
            plainFont.method382(0xffffff, 184, "Private chat", 28, true);
            if (privateChatMode == 0)
                plainFont.method382(65280, 184, "On", 41, true);
            if (privateChatMode == 1)
                plainFont.method382(0xffff00, 184, "Friends", 41, true);
            if (privateChatMode == 2)
                plainFont.method382(0xff0000, 184, "Off", 41, true);
            plainFont.method382(0xffffff, 324, "Trade/compete", 28, true);
            if (tradeMode == 0)
                plainFont.method382(65280, 324, "On", 41, true);
            if (tradeMode == 1)
                plainFont.method382(0xffff00, 324, "Friends", 41, true);
            if (tradeMode == 2)
                plainFont.method382(0xff0000, 324, "Off", 41, true);
            plainFont.method382(0xffffff, 458, "Report abuse", 33, true);
            aRSImageProducer_1123.drawGraphics(453, super.graphics, 0);
            gameScreenCanvas.initDrawingArea();
        }
        anInt945 = 0;
    }

    private boolean buildFriendsListMenu(RSInterface class9) {
        int i = class9.content_type;
        if (i >= 1 && i <= 200 || i >= 701 && i <= 900) {
            if (i >= 801)
                i -= 701;
            else if (i >= 701)
                i -= 601;
            else if (i >= 101)
                i -= 101;
            else
                i--;
            menuActionName[menuActionRow] = "Remove @whi@" + user_friends_name_string[i];
            menuActionID[menuActionRow] = 792;
            menuActionRow++;
            menuActionName[menuActionRow] = "Message @whi@" + user_friends_name_string[i];
            menuActionID[menuActionRow] = 639;
            menuActionRow++;
            return true;
        }
        if (i >= 401 && i <= 500) {
            menuActionName[menuActionRow] = "Remove @whi@" + class9.text_conditionfalse;
            menuActionID[menuActionRow] = 322;
            menuActionRow++;
            return true;
        } else {
            return false;
        }
    }

    private void renderStationaryGraphics() {
        StillGraphic class30_sub2_sub4_sub3 = (StillGraphic) aClass19_1056.getFront();
        for (; class30_sub2_sub4_sub3 != null; class30_sub2_sub4_sub3 = (StillGraphic) aClass19_1056.getNext())
            if (class30_sub2_sub4_sub3.anInt1560 != plane || class30_sub2_sub4_sub3.aBoolean1567)
                class30_sub2_sub4_sub3.unlink();
            else if (currentTime >= class30_sub2_sub4_sub3.anInt1564) {
                class30_sub2_sub4_sub3.method454(anInt945);
                if (class30_sub2_sub4_sub3.aBoolean1567)
                    class30_sub2_sub4_sub3.unlink();
                else
                    sceneGraph.addEntityA(class30_sub2_sub4_sub3.anInt1560, 0, class30_sub2_sub4_sub3.anInt1563, -1, class30_sub2_sub4_sub3.anInt1562, 60, class30_sub2_sub4_sub3.anInt1561, class30_sub2_sub4_sub3, false);
            }

    }

    private void interface_render(int x, int y, int scroll_position, RSInterface inter) {
        if (inter.type != 0 || inter.children == null)
            return;
        if (inter.hidden_until_mouseover && anInt1026 != inter.id && anInt1048 != inter.id && anInt1039 != inter.id)
            return;
        int i1 = Graphics2D.topX;
        int j1 = Graphics2D.topY;
        int k1 = Graphics2D.viewport_w;
        int l1 = Graphics2D.viewport_h;
        Graphics2D.setBounds(scroll_position + inter.height, x, x + inter.width, scroll_position);
        int i2 = inter.children.length;
        for (int j2 = 0; j2 < i2; j2++) {
            int k2 = inter.childX[j2] + x;
            int l2 = (inter.childY[j2] + scroll_position) - y;
            RSInterface class9_1 = RSInterface.interfaceCache[inter.children[j2]];
            k2 += class9_1.x_offset;
            l2 += class9_1.y_offset;
            if (class9_1.content_type > 0)
                interface_handle_auto_content(class9_1);
            if (class9_1.type == 0) {
                if (class9_1.scrollPosition > class9_1.scrollMax - class9_1.height)
                    class9_1.scrollPosition = class9_1.scrollMax - class9_1.height;
                if (class9_1.scrollPosition < 0)
                    class9_1.scrollPosition = 0;
                interface_render(k2, class9_1.scrollPosition, l2, class9_1);
                if (class9_1.scrollMax > class9_1.height)
                    renderChatInterface(class9_1.height, class9_1.scrollPosition, l2, k2 + class9_1.width, class9_1.scrollMax);
            } else if (class9_1.type != 1)
                if (class9_1.type == 2) {
                    int i3 = 0;
                    for (int l3 = 0; l3 < class9_1.height; l3++) {
                        for (int l4 = 0; l4 < class9_1.width; l4++) {
                            int k5 = k2 + l4 * (32 + class9_1.invSpritePadX);
                            int j6 = l2 + l3 * (32 + class9_1.invSpritePadY);
                            if (i3 < 20) {
                                k5 += class9_1.spritesX[i3];
                                j6 += class9_1.spritesY[i3];
                            }
                            if (class9_1.inv[i3] > 0) {
                                int k6 = 0;
                                int j7 = 0;
                                int j9 = class9_1.inv[i3] - 1;
                                if (k5 > Graphics2D.topX - 32 && k5 < Graphics2D.viewport_w && j6 > Graphics2D.topY - 32 && j6 < Graphics2D.viewport_h || activeInterfaceType != 0 && anInt1085 == i3) {
                                    int l9 = 0;
                                    if (itemSelected == 1 && anInt1283 == i3 && anInt1284 == class9_1.id)
                                        l9 = 0xffffff;
                                    RgbImage class30_sub2_sub1_sub1_2 = ItemDef.getSprite(j9, class9_1.invStackSizes[i3], l9);
                                    if (class30_sub2_sub1_sub1_2 != null) {
                                        if (activeInterfaceType != 0 && anInt1085 == i3 && anInt1084 == class9_1.id) {
                                            k6 = super.mouseEventX - anInt1087;
                                            j7 = super.mouseEventY - anInt1088;
                                            if (k6 < 5 && k6 > -5)
                                                k6 = 0;
                                            if (j7 < 5 && j7 > -5)
                                                j7 = 0;
                                            if (anInt989 < 5) {
                                                k6 = 0;
                                                j7 = 0;
                                            }
                                            class30_sub2_sub1_sub1_2.drawSprite1(k5 + k6, j6 + j7);
                                            if (j6 + j7 < Graphics2D.topY && inter.scrollPosition > 0) {
                                                int i10 = (anInt945 * (Graphics2D.topY - j6 - j7)) / 3;
                                                if (i10 > anInt945 * 10)
                                                    i10 = anInt945 * 10;
                                                if (i10 > inter.scrollPosition)
                                                    i10 = inter.scrollPosition;
                                                inter.scrollPosition -= i10;
                                                anInt1088 += i10;
                                            }
                                            if (j6 + j7 + 32 > Graphics2D.viewport_h && inter.scrollPosition < inter.scrollMax - inter.height) {
                                                int j10 = (anInt945 * ((j6 + j7 + 32) - Graphics2D.viewport_h)) / 3;
                                                if (j10 > anInt945 * 10)
                                                    j10 = anInt945 * 10;
                                                if (j10 > inter.scrollMax - inter.height - inter.scrollPosition)
                                                    j10 = inter.scrollMax - inter.height - inter.scrollPosition;
                                                inter.scrollPosition += j10;
                                                anInt1088 -= j10;
                                            }
                                        } else if (atInventoryInterfaceType != 0 && atInventoryIndex == i3 && atInventoryInterface == class9_1.id)
                                            class30_sub2_sub1_sub1_2.drawSprite1(k5, j6);
                                        else
                                            class30_sub2_sub1_sub1_2.drawSprite(k5, j6);
                                        if (class30_sub2_sub1_sub1_2.w2 == 33 || class9_1.invStackSizes[i3] != 1) {
                                            int k10 = class9_1.invStackSizes[i3];
                                            smallFont.drawTextHLeftVMid(0, logic_get_amount_string(k10), j6 + 10 + j7, k5 + 1 + k6);
                                            smallFont.drawTextHLeftVMid(0xffff00, logic_get_amount_string(k10), j6 + 9 + j7, k5 + k6);
                                        }
                                    }
                                }
                            } else if (class9_1.rgbImages != null && i3 < 20) {
                                RgbImage class30_sub2_sub1_sub1_1 = class9_1.rgbImages[i3];
                                if (class30_sub2_sub1_sub1_1 != null)
                                    class30_sub2_sub1_sub1_1.drawSprite(k5, j6);
                            }
                            i3++;
                        }

                    }

                } else if (class9_1.type == 3) {
                    boolean flag = false;
                    if (anInt1039 == class9_1.id || anInt1048 == class9_1.id || anInt1026 == class9_1.id)
                        flag = true;
                    int j3;
                    if (interface_test_condition(class9_1)) {
                        j3 = class9_1.colour_conditiontrue;
                        if (flag && class9_1.colour_conditiontrue_mouseover != 0)
                            j3 = class9_1.colour_conditiontrue_mouseover;
                    } else {
                        j3 = class9_1.colour_conditionfalse;
                        if (flag && class9_1.colour_conditionfalse_mouseover != 0)
                            j3 = class9_1.colour_conditionfalse_mouseover;
                    }
                    if (class9_1.aByte254 == 0) {
                        if (class9_1.filled)
                            Graphics2D.fillRect(k2, l2, class9_1.width, class9_1.height, j3);
                        else
                            Graphics2D.drawRect(k2, l2, class9_1.width, class9_1.height, j3);
                    } else if (class9_1.filled)
                        Graphics2D.fillRect(k2, l2, class9_1.width, class9_1.height, j3, 256 - (class9_1.aByte254 & 0xff));
                    else
                        Graphics2D.drawRect(k2, l2, class9_1.width, class9_1.height, j3, 256 - (class9_1.aByte254 & 0xff));
                } else if (class9_1.type == 4) {
                    RSFont RSFont = class9_1.font;
                    String s = class9_1.text_conditionfalse;
                    boolean flag1 = false;
                    if (anInt1039 == class9_1.id || anInt1048 == class9_1.id || anInt1026 == class9_1.id)
                        flag1 = true;
                    int i4;
                    if (interface_test_condition(class9_1)) {
                        i4 = class9_1.colour_conditiontrue;
                        if (flag1 && class9_1.colour_conditiontrue_mouseover != 0)
                            i4 = class9_1.colour_conditiontrue_mouseover;
                        if (class9_1.text_conditiontrue.length() > 0)
                            s = class9_1.text_conditiontrue;
                    } else {
                        i4 = class9_1.colour_conditionfalse;
                        if (flag1 && class9_1.colour_conditionfalse_mouseover != 0)
                            i4 = class9_1.colour_conditionfalse_mouseover;
                    }
                    if (class9_1.atActionType == 6 && aBoolean1149) {
                        s = "Please wait...";
                        i4 = class9_1.colour_conditionfalse;
                    }
                    if (Graphics2D.width == 479) {
                        if (i4 == 0xffff00)
                            i4 = 255;
                        if (i4 == 49152)
                            i4 = 0xffffff;
                    }
                    for (int l6 = l2 + RSFont.charHeight; s.length() > 0; l6 += RSFont.charHeight) {
                        if (s.indexOf("%") != -1) {
                            do {
                                int k7 = s.indexOf("%1");
                                if (k7 == -1)
                                    break;
                                s = s.substring(0, k7) + interfaceIntToString(interface_calculate_dynamic_value(class9_1, 0)) + s.substring(k7 + 2);
                            } while (true);
                            do {
                                int l7 = s.indexOf("%2");
                                if (l7 == -1)
                                    break;
                                s = s.substring(0, l7) + interfaceIntToString(interface_calculate_dynamic_value(class9_1, 1)) + s.substring(l7 + 2);
                            } while (true);
                            do {
                                int i8 = s.indexOf("%3");
                                if (i8 == -1)
                                    break;
                                s = s.substring(0, i8) + interfaceIntToString(interface_calculate_dynamic_value(class9_1, 2)) + s.substring(i8 + 2);
                            } while (true);
                            do {
                                int j8 = s.indexOf("%4");
                                if (j8 == -1)
                                    break;
                                s = s.substring(0, j8) + interfaceIntToString(interface_calculate_dynamic_value(class9_1, 3)) + s.substring(j8 + 2);
                            } while (true);
                            do {
                                int k8 = s.indexOf("%5");
                                if (k8 == -1)
                                    break;
                                s = s.substring(0, k8) + interfaceIntToString(interface_calculate_dynamic_value(class9_1, 4)) + s.substring(k8 + 2);
                            } while (true);
                        }
                        int l8 = s.indexOf("\\n");
                        String s1;
                        if (l8 != -1) {
                            s1 = s.substring(0, l8);
                            s = s.substring(l8 + 2);
                        } else {
                            s1 = s;
                            s = "";
                        }
                        if (class9_1.text_centered)
                            RSFont.method382(i4, k2 + class9_1.width / 2, s1, l6, class9_1.text_shadow);
                        else
                            RSFont.method389(class9_1.text_shadow, k2, i4, s1, l6);
                    }

                } else if (class9_1.type == 5) {
                    RgbImage rgbImage;
                    if (interface_test_condition(class9_1))
                        rgbImage = class9_1.image_conditiontrue;
                    else
                        rgbImage = class9_1.image_conditionfalse;
                    if (rgbImage != null)
                        rgbImage.drawSprite(k2, l2);
                } else if (class9_1.type == 6) {
                    int k3 = Rasterizer.centerX;
                    int j4 = Rasterizer.centerY;
                    Rasterizer.centerX = k2 + class9_1.width / 2;
                    Rasterizer.centerY = l2 + class9_1.height / 2;
                    int i5 = Rasterizer.SINE[class9_1.anInt270] * class9_1.anInt269 >> 16;
                    int l5 = Rasterizer.COSINE[class9_1.anInt270] * class9_1.anInt269 >> 16;
                    boolean flag2 = interface_test_condition(class9_1);
                    int i7;
                    if (flag2)
                        i7 = class9_1.animation_conditiontrue;
                    else
                        i7 = class9_1.animation_conditionfalse;
                    Model model;
                    if (i7 == -1) {
                        model = class9_1.method209(-1, -1, flag2);
                    } else {
                        Sequence sequence = Sequence.anims[i7];
                        model = class9_1.method209(sequence.frame1IDS[class9_1.anim_frame], sequence.frame2IDS[class9_1.anim_frame], flag2);
                    }
                    if (model != null)
                        model.rendersingle(class9_1.anInt271, 0, class9_1.anInt270, 0, i5, l5);
                    Rasterizer.centerX = k3;
                    Rasterizer.centerY = j4;
                } else if (class9_1.type == 7) {
                    RSFont RSFont_1 = class9_1.font;
                    int k4 = 0;
                    for (int j5 = 0; j5 < class9_1.height; j5++) {
                        for (int i6 = 0; i6 < class9_1.width; i6++) {
                            if (class9_1.inv[k4] > 0) {
                                ItemDef itemDef = ItemDef.forID(class9_1.inv[k4] - 1);
                                String s2 = itemDef.name;
                                if (itemDef.stackable || class9_1.invStackSizes[k4] != 1)
                                    s2 = s2 + " x" + logicGetAmountString(class9_1.invStackSizes[k4]);
                                int i9 = k2 + i6 * (115 + class9_1.invSpritePadX);
                                int k9 = l2 + j5 * (12 + class9_1.invSpritePadY);
                                if (class9_1.text_centered)
                                    RSFont_1.method382(class9_1.colour_conditionfalse, i9 + class9_1.width / 2, s2, k9, class9_1.text_shadow);
                                else
                                    RSFont_1.method389(class9_1.text_shadow, i9, class9_1.colour_conditionfalse, s2, k9);
                            }
                            k4++;
                        }

                    }

                }
        }

        Graphics2D.setBounds(l1, i1, k1, j1);
    }

    private void randomizeBackground(IndexedImage indexedImage) {
        int j = 256;
        for (int k = 0; k < anIntArray1190.length; k++)
            anIntArray1190[k] = 0;

        for (int l = 0; l < 5000; l++) {
            int i1 = (int) (Math.random() * 128D * (double) j);
            anIntArray1190[i1] = (int) (Math.random() * 256D);
        }

        for (int j1 = 0; j1 < 20; j1++) {
            for (int k1 = 1; k1 < j - 1; k1++) {
                for (int i2 = 1; i2 < 127; i2++) {
                    int k2 = i2 + (k1 << 7);
                    anIntArray1191[k2] = (anIntArray1190[k2 - 1] + anIntArray1190[k2 + 1] + anIntArray1190[k2 - 128] + anIntArray1190[k2 + 128]) / 4;
                }

            }

            int ai[] = anIntArray1190;
            anIntArray1190 = anIntArray1191;
            anIntArray1191 = ai;
        }

        if (indexedImage != null) {
            int l1 = 0;
            for (int j2 = 0; j2 < indexedImage.imgHeight; j2++) {
                for (int l2 = 0; l2 < indexedImage.imgWidth; l2++)
                    if (indexedImage.imgPixels[l1++] != 0) {
                        int i3 = l2 + 16 + indexedImage.xDrawOffset;
                        int j3 = j2 + 16 + indexedImage.yDrawOffset;
                        int k3 = i3 + (j3 << 7);
                        anIntArray1190[k3] = 0;
                    }

            }

        }
    }

    private void appendPlayerUpdateMask(int i, int j, Packet stream, Player player) {
        if ((i & 0x400) != 0) {
            player.anInt1543 = stream.sg1();
            player.anInt1545 = stream.sg1();
            player.anInt1544 = stream.sg1();
            player.anInt1546 = stream.sg1();
            player.anInt1547 = stream.isg2() + currentTime;
            player.anInt1548 = stream.sg2() + currentTime;
            player.anInt1549 = stream.sg1();
            player.method446();
        }
        if ((i & 0x100) != 0) {
            player.anInt1520 = stream.ig2();
            int k = stream.g4();
            player.anInt1524 = k >> 16;
            player.anInt1523 = currentTime + (k & 0xffff);
            player.anInt1521 = 0;
            player.anInt1522 = 0;
            if (player.anInt1523 > currentTime)
                player.anInt1521 = -1;
            if (player.anInt1520 == 65535)
                player.anInt1520 = -1;
        }
        if ((i & 8) != 0) {
            int l = stream.ig2();
            if (l == 65535)
                l = -1;
            int i2 = stream.ng1b();
            if (l == player.animation && l != -1) {
                int i3 = Sequence.anims[l].anInt365;
                if (i3 == 1) {
                    player.anInt1527 = 0;
                    player.anInt1528 = 0;
                    player.anInt1529 = i2;
                    player.anInt1530 = 0;
                }
                if (i3 == 2)
                    player.anInt1530 = 0;
            } else if (l == -1 || player.animation == -1 || Sequence.anims[l].anInt359 >= Sequence.anims[player.animation].anInt359) {
                player.animation = l;
                player.anInt1527 = 0;
                player.anInt1528 = 0;
                player.anInt1529 = i2;
                player.anInt1530 = 0;
                player.anInt1542 = player.pathLength;
            }
        }
        if ((i & 4) != 0) {
            player.textSpoken = stream.gstr();
            if (player.textSpoken.charAt(0) == '~') {
                player.textSpoken = player.textSpoken.substring(1);
                pushMessage(player.textSpoken, 2, player.name);
            } else if (player == session_player)
                pushMessage(player.textSpoken, 2, player.name);
            player.anInt1513 = 0;
            player.anInt1531 = 0;
            player.textCycle = 150;
        }
        if ((i & 0x80) != 0) {
            int i1 = stream.ig2();
            int j2 = stream.g1();
            int j3 = stream.ng1b();
            int k3 = stream.pos;
            if (player.name != null && player.visible) {
                long l3 = TextClass.longForName(player.name);
                boolean flag = false;
                if (j2 <= 1) {
                    for (int i4 = 0; i4 < user_ignore_count; i4++) {
                        if (user_ignore_names[i4] != l3)
                            continue;
                        flag = true;
                        break;
                    }

                }
                if (!flag && anInt1251 == 0)
                    try {
                        aStream_834.pos = 0;
                        stream.igdata(aStream_834.data, 0, j3);
                        aStream_834.pos = 0;
                        String s = TextInput.method525(j3, aStream_834);
                        s = Censor.doCensor(s);
                        player.textSpoken = s;
                        player.anInt1513 = i1 >> 8;
                        player.privelage = j2;

                        //entityMessage(player);

                        player.anInt1531 = i1 & 0xff;
                        player.textCycle = 150;
                        if (j2 == 2 || j2 == 3)
                            pushMessage(s, 1, "@cr2@" + player.name);
                        else if (j2 == 1)
                            pushMessage(s, 1, "@cr1@" + player.name);
                        else
                            pushMessage(s, 2, player.name);
                    } catch (Exception exception) {
                        Signlink.reporterror("cde2");
                    }
            }
            stream.pos = k3 + j3;
        }
        if ((i & 1) != 0) {
            player.interactingEntity = stream.ig2();
            if (player.interactingEntity == 65535)
                player.interactingEntity = -1;
        }
        if ((i & 0x10) != 0) {
            int j1 = stream.ng1b();
            byte abyte0[] = new byte[j1];
            Packet stream_1 = new Packet(abyte0);
            stream.gdata(abyte0, 0, j1);
            aStreamArray895s[j] = stream_1;
            player.updatePlayer(stream_1);
        }
        if ((i & 2) != 0) {
            player.anInt1538 = stream.isg2();
            player.anInt1539 = stream.ig2();
        }
        if ((i & 0x20) != 0) {
            int k1 = stream.g1();
            int k2 = stream.nsp1();
            player.updateHitData(k2, k1, currentTime);
            player.loopCycleStatus = currentTime + 300;
            player.currentHealth = stream.ng1b();
            player.maxHealth = stream.g1();
        }
        if ((i & 0x200) != 0) {
            int l1 = stream.g1();
            int l2 = stream.sg1();
            player.updateHitData(l2, l1, currentTime);
            player.loopCycleStatus = currentTime + 300;
            player.currentHealth = stream.g1();
            player.maxHealth = stream.ng1b();
        }
    }

    private void method108() {
        try {
            int j = session_player.boundExtentX + anInt1278;
            int k = session_player.boundExtentY + anInt1131;
            if (anInt1014 - j < -500 || anInt1014 - j > 500 || anInt1015 - k < -500 || anInt1015 - k > 500) {
                anInt1014 = j;
                anInt1015 = k;
            }
            if (anInt1014 != j)
                anInt1014 += (j - anInt1014) / 16;
            if (anInt1015 != k)
                anInt1015 += (k - anInt1015) / 16;
            if (super.keyStatus[1] == 1)
                anInt1186 += (-24 - anInt1186) / 2;
            else if (super.keyStatus[2] == 1)
                anInt1186 += (24 - anInt1186) / 2;
            else
                anInt1186 /= 2;
            if (super.keyStatus[3] == 1)
                anInt1187 += (12 - anInt1187) / 2;
            else if (super.keyStatus[4] == 1)
                anInt1187 += (-12 - anInt1187) / 2;
            else
                anInt1187 /= 2;
            minimapInt1 = minimapInt1 + anInt1186 / 2 & 0x7ff;
            anInt1184 += anInt1187 / 2;
            if (anInt1184 < 128)
                anInt1184 = 128;
            if (anInt1184 > 383)
                anInt1184 = 383;
            int l = anInt1014 >> 7;
            int i1 = anInt1015 >> 7;
            int j1 = method42(plane, anInt1015, anInt1014);
            int k1 = 0;
            if (l > 3 && i1 > 3 && l < 100 && i1 < 100) {
                for (int l1 = l - 4; l1 <= l + 4; l1++) {
                    for (int k2 = i1 - 4; k2 <= i1 + 4; k2++) {
                        int l2 = plane;
                        if (l2 < 3 && (tileSettingBits[1][l1][k2] & 2) == 2)
                            l2++;
                        int i3 = j1 - intGroundArray[l2][l1][k2];
                        if (i3 > k1)
                            k1 = i3;
                    }

                }

            }
            anInt1005++;
            if (anInt1005 > 1512) {
                anInt1005 = 0;
                stream.p1isaac(77);
                stream.p1(0);
                int i2 = stream.pos;
                stream.p1((int) (Math.random() * 256D));
                stream.p1(101);
                stream.p1(233);
                stream.p2(45092);
                if ((int) (Math.random() * 2D) == 0)
                    stream.p2(35784);
                stream.p1((int) (Math.random() * 256D));
                stream.p1(64);
                stream.p1(38);
                stream.p2((int) (Math.random() * 65536D));
                stream.p2((int) (Math.random() * 65536D));
                stream.psize1(stream.pos - i2);
            }
            int j2 = k1 * 192;
            if (j2 > 0x17f00)
                j2 = 0x17f00;
            if (j2 < 32768)
                j2 = 32768;
            if (j2 > anInt984) {
                anInt984 += (j2 - anInt984) / 24;
                return;
            }
            if (j2 < anInt984) {
                anInt984 += (j2 - anInt984) / 80;
            }
        } catch (Exception _ex) {
            Signlink.reporterror("glfc_ex " + session_player.boundExtentX + "," + session_player.boundExtentY + "," + anInt1014 + "," + anInt1015 + "," + anInt1069 + "," + anInt1070 + "," + baseX + "," + baseY);
            throw new RuntimeException("eek");
        }
    }

    public void drawGame() {
        if (rsAlreadyLoaded || loadingError || genericLoadingError) {
            showErrorScreen();
            return;
        }
        anInt1061++;
        if (!loggedIn)
            drawLoginScreen(false, 0);
        else
            drawGameScreen();
        anInt1213 = 0;
    }

    private boolean isFriendOrSelf(String s) {
        if (s == null)
            return false;
        for (int i = 0; i < user_friends_count; i++)
            if (s.equalsIgnoreCase(user_friends_name_string[i]))
                return true;
        return s.equalsIgnoreCase(session_player.name);
    }

    private static String logic_get_combat_risk_colour(int i, int j) {
        int k = i - j;
        if (k < -9)
            return "@red@";
        if (k < -6)
            return "@or3@";
        if (k < -3)
            return "@or2@";
        if (k < 0)
            return "@or1@";
        if (k > 9)
            return "@gre@";
        if (k > 6)
            return "@gr3@";
        if (k > 3)
            return "@gr2@";
        if (k > 0)
            return "@gr1@";
        else
            return "@yel@";
    }

    private void setWaveVolume(int i) {
        Signlink.wavevol = i;
    }

    private void draw3dScreen() {
        drawSplitPrivateChat();
        if (crossType == 1) {
            crosses[crossIndex / 100].drawSprite(crossX - 8 - 4, crossY - 8 - 4);
            anInt1142++;
            if (anInt1142 > 67) {
                anInt1142 = 0;
                stream.p1isaac(78);
            }
        }
        if (crossType == 2)
            crosses[4 + crossIndex / 100].drawSprite(crossX - 8 - 4, crossY - 8 - 4);
        if (anInt1018 != -1) {
            animateRSInterface(anInt945, anInt1018);
            interface_render(0, 0, 0, RSInterface.interfaceCache[anInt1018]);
        }
        if (openInterfaceID != -1) {
            animateRSInterface(anInt945, openInterfaceID);
            interface_render(0, 0, 0, RSInterface.interfaceCache[openInterfaceID]);
        }
        checkTutorialIsland();
        if (!menuOpen) {
            processRightClick();
            drawTooltip();
        } else if (menuScreenArea == 0)
            drawMenu();
        if (anInt1055 == 1)
            headIcons[1].drawSprite(472, 296);
        if (fpsOn) {
            char c = '\u01FB';
            int k = 20;
            int i1 = 0xffff00;
            if (super.fps < 15)
                i1 = 0xff0000;
            plainFont.method380("Fps:" + super.fps, c, i1, k);
            k += 15;
            Runtime runtime = Runtime.getRuntime();
            int j1 = (int) ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024L));
            i1 = 0xffff00;
            if (j1 > 0x2000000 && lowMem)
                i1 = 0xff0000;
            plainFont.method380("Heap usage:" + j1 + "m", c, 0xffff00, k);
            k += 15;
            plainFont.method380("Card usage :" + ((ServerMemoryManager.arbBufferMemory + ServerMemoryManager.textureMemory) / (1024 * 1024L)) + "m", c, i1, k);
            k += 15;
        }
        if (anInt1104 != 0) {
            int j = anInt1104 / 50;
            int l = j / 60;
            j %= 60;
            if (j < 10)
                plainFont.drawTextHLeftVMid(0xffff00, "System update in: " + l + ":0" + j, 329, 4);
            else
                plainFont.drawTextHLeftVMid(0xffff00, "System update in: " + l + ":" + j, 329, 4);
            anInt849++;
            if (anInt849 > 75) {
                anInt849 = 0;
                stream.p1isaac(148);
            }
        }
    }

    private void addIgnore(long l) {
        try {
            if (l == 0L)
                return;
            if (user_ignore_count >= 100) {
                pushMessage("Your ignore list is full. Max of 100 hit", 0, "");
                return;
            }
            String s = TextClass.fixName(TextClass.nameForLong(l));
            for (int j = 0; j < user_ignore_count; j++)
                if (user_ignore_names[j] == l) {
                    pushMessage(s + " is already on your ignore list", 0, "");
                    return;
                }
            for (int k = 0; k < user_friends_count; k++)
                if (friendsListAsLongs[k] == l) {
                    pushMessage("Please remove " + s + " from your friend list first", 0, "");
                    return;
                }

            user_ignore_names[user_ignore_count++] = l;
            needDrawTabArea = true;
            stream.p1isaac(133);
            stream.p8(l);
            return;
        } catch (RuntimeException runtimeexception) {
            Signlink.reporterror("45688, " + l + ", " + 4 + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    private void updatePlayerInstances() {
        for (int i = -1; i < session_player_count; i++) {
            int j;
            if (i == -1)
                j = session_player_idx;
            else
                j = session_player_list[i];
            Player player = session_players[j];
            if (player != null)
                entityUpdateBlock(player);
        }

    }

    private void method115() {
        if (loadingStage == 2) {
            for (GameObjectSpawnRequest gameObjectSpawnRequest = (GameObjectSpawnRequest) aClass19_1179.getFront(); gameObjectSpawnRequest != null; gameObjectSpawnRequest = (GameObjectSpawnRequest) aClass19_1179.getNext()) {
                if (gameObjectSpawnRequest.anInt1294 > 0)
                    gameObjectSpawnRequest.anInt1294--;
                if (gameObjectSpawnRequest.anInt1294 == 0) {
                    if (gameObjectSpawnRequest.id < 0 || MapRegion.method178(gameObjectSpawnRequest.id, gameObjectSpawnRequest.type)) {
                        method142(gameObjectSpawnRequest.z, gameObjectSpawnRequest.plane, gameObjectSpawnRequest.face, gameObjectSpawnRequest.type, gameObjectSpawnRequest.x, gameObjectSpawnRequest.anInt1296, gameObjectSpawnRequest.id);
                        gameObjectSpawnRequest.unlink();
                    }
                } else {
                    if (gameObjectSpawnRequest.anInt1302 > 0)
                        gameObjectSpawnRequest.anInt1302--;
                    if (gameObjectSpawnRequest.anInt1302 == 0 && gameObjectSpawnRequest.x >= 1 && gameObjectSpawnRequest.z >= 1 && gameObjectSpawnRequest.x <= 102 && gameObjectSpawnRequest.z <= 102 && (gameObjectSpawnRequest.id2 < 0 || MapRegion.method178(gameObjectSpawnRequest.id2, gameObjectSpawnRequest.type2))) {
                        method142(gameObjectSpawnRequest.z, gameObjectSpawnRequest.plane, gameObjectSpawnRequest.face2, gameObjectSpawnRequest.type2, gameObjectSpawnRequest.x, gameObjectSpawnRequest.anInt1296, gameObjectSpawnRequest.id2);
                        gameObjectSpawnRequest.anInt1302 = -1;
                        if (gameObjectSpawnRequest.id2 == gameObjectSpawnRequest.id && gameObjectSpawnRequest.id == -1)
                            gameObjectSpawnRequest.unlink();
                        else if (gameObjectSpawnRequest.id2 == gameObjectSpawnRequest.id && gameObjectSpawnRequest.face2 == gameObjectSpawnRequest.face && gameObjectSpawnRequest.type2 == gameObjectSpawnRequest.type)
                            gameObjectSpawnRequest.unlink();
                    }
                }
            }

        }
    }

    private void determineMenuSize() {
        int i = boldFont.getTextWidth("Choose Option");
        for (int j = 0; j < menuActionRow; j++) {
            int k = boldFont.getTextWidth(menuActionName[j]);
            if (k > i)
                i = k;
        }

        i += 8;
        int l = 15 * menuActionRow + 21;
        if (super.clickX > 4 && super.clickY > 4 && super.clickX < 516 && super.clickY < 338) {
            int i1 = super.clickX - 4 - i / 2;
            if (i1 + i > 512)
                i1 = 512 - i;
            if (i1 < 0)
                i1 = 0;
            int l1 = super.clickY - 4;
            if (l1 + l > 334)
                l1 = 334 - l;
            if (l1 < 0)
                l1 = 0;
            menuOpen = true;
            menuScreenArea = 0;
            menuOffsetX = i1;
            menuOffsetY = l1;
            menuWidth = i;
            anInt952 = 15 * menuActionRow + 22;
        }
        if (super.clickX > 553 && super.clickY > 205 && super.clickX < 743 && super.clickY < 466) {
            int j1 = super.clickX - 553 - i / 2;
            if (j1 < 0)
                j1 = 0;
            else if (j1 + i > 190)
                j1 = 190 - i;
            int i2 = super.clickY - 205;
            if (i2 < 0)
                i2 = 0;
            else if (i2 + l > 261)
                i2 = 261 - l;
            menuOpen = true;
            menuScreenArea = 1;
            menuOffsetX = j1;
            menuOffsetY = i2;
            menuWidth = i;
            anInt952 = 15 * menuActionRow + 22;
        }
        if (super.clickX > 17 && super.clickY > 357 && super.clickX < 496 && super.clickY < 453) {
            int k1 = super.clickX - 17 - i / 2;
            if (k1 < 0)
                k1 = 0;
            else if (k1 + i > 479)
                k1 = 479 - i;
            int j2 = super.clickY - 357;
            if (j2 < 0)
                j2 = 0;
            else if (j2 + l > 96)
                j2 = 96 - l;
            menuOpen = true;
            menuScreenArea = 2;
            menuOffsetX = k1;
            menuOffsetY = j2;
            menuWidth = i;
            anInt952 = 15 * menuActionRow + 22;
        }
    }

    private void updatePlayerMovement(Packet stream) {
        stream.begin_bit_block();
        int j = stream.gbits(1);
        if (j == 0)
            return;
        int k = stream.gbits(2);
        if (k == 0) {
            session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = session_player_idx;
            return;
        }
        if (k == 1) {
            int l = stream.gbits(3);
            session_player.move(false, l);
            int k1 = stream.gbits(1);
            if (k1 == 1)
                session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = session_player_idx;
            return;
        }
        if (k == 2) {
            int i1 = stream.gbits(3);
            session_player.move(true, i1);
            int l1 = stream.gbits(3);
            session_player.move(true, l1);
            int j2 = stream.gbits(1);
            if (j2 == 1)
                session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = session_player_idx;
            return;
        }
        if (k == 3) {
            plane = stream.gbits(2);
            int j1 = stream.gbits(1);
            int i2 = stream.gbits(1);
            if (i2 == 1)
                session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = session_player_idx;
            int k2 = stream.gbits(7);
            int l2 = stream.gbits(7);
            session_player.setPos(l2, k2, j1 == 1);
        }
    }

    private void clearCache() {
        aBoolean831 = false;
        while (drawingFlames) {
            aBoolean831 = false;
            try {
                Thread.sleep(50L);
            } catch (Exception _ex) {
            }
        }
        //aIndexedImage_966 = null;//never used
        //aIndexedImage_967 = null;//never used
        //aIndexedImage_999 = null;//never used
        titlebox = null;
        loginhover = null;
        passwordhover = null;
        loadingBox1 = null;
        aIndexedImageArray1152s = null;
        anIntArray850 = null;
        anIntArray851 = null;
        anIntArray852 = null;
        anIntArray853 = null;
        anIntArray1190 = null;
        anIntArray1191 = null;
        anIntArray828 = null;
        anIntArray829 = null;
        aClass30_Sub2_Sub1_Sub1_1201 = null;
        aClass30_Sub2_Sub1_Sub1_1202 = null;
    }

    private boolean animateRSInterface(int i, int j) {
        boolean flag1 = false;
        RSInterface class9 = RSInterface.interfaceCache[j];
        for (int k = 0; k < class9.children.length; k++) {
            if (class9.children[k] == -1)
                break;
            RSInterface class9_1 = RSInterface.interfaceCache[class9.children[k]];
            if (class9_1.type == 1)
                flag1 |= animateRSInterface(i, class9_1.id);
            if (class9_1.type == 6 && (class9_1.animation_conditionfalse != -1 || class9_1.animation_conditiontrue != -1)) {
                boolean flag2 = interface_test_condition(class9_1);
                int l;
                if (flag2)
                    l = class9_1.animation_conditiontrue;
                else
                    l = class9_1.animation_conditionfalse;
                if (l != -1) {
                    Sequence sequence = Sequence.anims[l];
                    for (class9_1.anInt208 += i; class9_1.anInt208 > sequence.getFrameLength(class9_1.anim_frame); ) {
                        class9_1.anInt208 -= sequence.getFrameLength(class9_1.anim_frame) + 1;
                        class9_1.anim_frame++;
                        if (class9_1.anim_frame >= sequence.frameCount) {
                            class9_1.anim_frame -= sequence.frameStep;
                            if (class9_1.anim_frame < 0 || class9_1.anim_frame >= sequence.frameCount)
                                class9_1.anim_frame = 0;
                        }
                        flag1 = true;
                    }

                }
            }
        }

        return flag1;
    }

    private int method120() {
        int j = 3;
        if (yCameraCurve < 310) {
            int k = xCameraPos >> 7;
            int l = yCameraPos >> 7;
            int i1 = session_player.boundExtentX >> 7;
            int j1 = session_player.boundExtentY >> 7;
            if ((tileSettingBits[plane][k][l] & 4) != 0)
                j = plane;
            int k1;
            if (i1 > k)
                k1 = i1 - k;
            else
                k1 = k - i1;
            int l1;
            if (j1 > l)
                l1 = j1 - l;
            else
                l1 = l - j1;
            if (k1 > l1) {
                int i2 = (l1 * 0x10000) / k1;
                int k2 = 32768;
                while (k != i1) {
                    if (k < i1)
                        k++;
                    else if (k > i1)
                        k--;
                    if ((tileSettingBits[plane][k][l] & 4) != 0)
                        j = plane;
                    k2 += i2;
                    if (k2 >= 0x10000) {
                        k2 -= 0x10000;
                        if (l < j1)
                            l++;
                        else if (l > j1)
                            l--;
                        if ((tileSettingBits[plane][k][l] & 4) != 0)
                            j = plane;
                    }
                }
            } else {
                int j2 = (k1 * 0x10000) / l1;
                int l2 = 32768;
                while (l != j1) {
                    if (l < j1)
                        l++;
                    else if (l > j1)
                        l--;
                    if ((tileSettingBits[plane][k][l] & 4) != 0)
                        j = plane;
                    l2 += j2;
                    if (l2 >= 0x10000) {
                        l2 -= 0x10000;
                        if (k < i1)
                            k++;
                        else if (k > i1)
                            k--;
                        if ((tileSettingBits[plane][k][l] & 4) != 0)
                            j = plane;
                    }
                }
            }
        }
        if ((tileSettingBits[plane][session_player.boundExtentX >> 7][session_player.boundExtentY >> 7] & 4) != 0)
            j = plane;
        return j;
    }

    private int resetCameraHeight() {
        int j = method42(plane, yCameraPos, xCameraPos);
        if (j - zCameraPos < 800 && (tileSettingBits[plane][xCameraPos >> 7][yCameraPos >> 7] & 4) != 0)
            return plane;
        else
            return 3;
    }

    private void delIgnore(long l) {
        try {
            if (l == 0L)
                return;
            for (int j = 0; j < user_ignore_count; j++)
                if (user_ignore_names[j] == l) {
                    user_ignore_count--;
                    needDrawTabArea = true;
                    System.arraycopy(user_ignore_names, j + 1, user_ignore_names, j, user_ignore_count - j);

                    stream.p1isaac(74);
                    stream.p8(l);
                    return;
                }

            return;
        } catch (RuntimeException runtimeexception) {
            Signlink.reporterror("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    public String getParameter(String s) {
        if (Signlink.mainapp != null)
            return Signlink.mainapp.getParameter(s);
        else
            return super.getParameter(s);
    }

    private void adjustVolume(boolean flag, int i) {
        Signlink.midivol = i;
        if (flag)
            Signlink.midi = "voladjust";
    }

    private int interface_calculate_dynamic_value(RSInterface class9, int j) {
        if (class9.dynamic_value_formulas == null || j >= class9.dynamic_value_formulas.length)
            return -2;
        try {
            int ai[] = class9.dynamic_value_formulas[j];
            int k = 0;
            int l = 0;
            int i1 = 0;
            do {
                int j1 = ai[l++];
                int k1 = 0;
                byte byte0 = 0;
                if (j1 == 0)
                    return k;
                if (j1 == 1)
                    k1 = user_levels[ai[l++]];
                if (j1 == 2)
                    k1 = user_stats[ai[l++]];
                if (j1 == 3)
                    k1 = user_experience[ai[l++]];
                if (j1 == 4) {
                    RSInterface class9_1 = RSInterface.interfaceCache[ai[l++]];
                    int k2 = ai[l++];
                    if (k2 >= 0 && k2 < ItemDef.totalItems && (!ItemDef.forID(k2).membersObject || isMembers)) {
                        for (int j3 = 0; j3 < class9_1.inv.length; j3++)
                            if (class9_1.inv[j3] == k2 + 1)
                                k1 += class9_1.invStackSizes[j3];

                    }
                }
                if (j1 == 5)
                    k1 = sessionSettings[ai[l++]];
                if (j1 == 6)
                    k1 = XP_FOR_LEVEL[user_stats[ai[l++]] - 1];
                if (j1 == 7)
                    k1 = (sessionSettings[ai[l++]] * 100) / 46875;
                if (j1 == 8)
                    k1 = session_player.combatLevel;
                if (j1 == 9) {
                    for (int l1 = 0; l1 < Skills.COUNT; l1++)
                        if (Skills.ENABLED[l1])
                            k1 += user_stats[l1];

                }
                if (j1 == 10) {
                    RSInterface class9_2 = RSInterface.interfaceCache[ai[l++]];
                    int l2 = ai[l++] + 1;
                    if (l2 >= 0 && l2 < ItemDef.totalItems && (!ItemDef.forID(l2).membersObject || isMembers)) {
                        for (int k3 = 0; k3 < class9_2.inv.length; k3++) {
                            if (class9_2.inv[k3] != l2)
                                continue;
                            k1 = 0x3b9ac9ff;
                            break;
                        }

                    }
                }
                if (j1 == 11)
                    k1 = user_energy;
                if (j1 == 12)
                    k1 = user_weight;
                if (j1 == 13) {
                    int i2 = sessionSettings[ai[l++]];
                    int i3 = ai[l++];
                    k1 = (i2 & 1 << i3) == 0 ? 0 : 1;
                }
                if (j1 == 14) {
                    int j2 = ai[l++];
                    VarBit varBit = VarBit.cache[j2];
                    int l3 = varBit.configId;
                    int i4 = varBit.leastSignificantBit;
                    int j4 = varBit.mostSignificantBit;
                    int k4 = StaticLogic.BITFIELD_MAX_VALUE[j4 - i4];
                    k1 = sessionSettings[l3] >> i4 & k4;
                }
                if (j1 == 15)
                    byte0 = 1;
                if (j1 == 16)
                    byte0 = 2;
                if (j1 == 17)
                    byte0 = 3;
                if (j1 == 18)
                    k1 = (session_player.boundExtentX >> 7) + baseX;
                if (j1 == 19)
                    k1 = (session_player.boundExtentY >> 7) + baseY;
                if (j1 == 20)
                    k1 = ai[l++];
                if (byte0 == 0) {
                    if (i1 == 0)
                        k += k1;
                    if (i1 == 1)
                        k -= k1;
                    if (i1 == 2 && k1 != 0)
                        k /= k1;
                    if (i1 == 3)
                        k *= k1;
                    i1 = 0;
                } else {
                    i1 = byte0;
                }
            } while (true);
        } catch (Exception _ex) {
            return -1;
        }
    }

    private void drawTooltip() {
        if (menuActionRow < 2 && itemSelected == 0 && spellSelected == 0)
            return;
        String s;
        if (itemSelected == 1 && menuActionRow < 2)
            s = "Use " + selectedItemName + " with...";
        else if (spellSelected == 1 && menuActionRow < 2)
            s = spellTooltip + "...";
        else
            s = menuActionName[menuActionRow - 1];
        if (menuActionRow > 2)
            s = s + "@whi@ / " + (menuActionRow - 2) + " more options";
        boldFont.method390(4, 0xffffff, s, currentTime / 1000, 15);
    }

    private void drawMinimap() {
        minimapIP.initDrawingArea();
        //drawStatusGlobes();
        if (miniMapLock == 2) {
            byte abyte0[] = mapBack.imgPixels;
            int ai[] = Graphics2D.pixels;
            int k2 = abyte0.length;
            for (int i5 = 0; i5 < k2; i5++)
                if (abyte0[i5] == 0)
                    ai[i5] = 0;

            compass.rotate(25, 25, 33, 33, minimapInt1, compassShape2, 256, compassShape1, 0, 0);

            gameScreenCanvas.initDrawingArea();
            return;
        }
        int i = minimapInt1 + minimapInt2 & 0x7ff;
        int j = 48 + session_player.boundExtentX / 32;
        int l2 = 464 - session_player.boundExtentY / 32;
        minimapImage.rotate(j, l2, 146, 151, i, minimapShape2, 256 + minimapInt3, minimapShape1, 5, 25);
        compass.rotate(25, 25, 33, 33, minimapInt1, compassShape2, 256, compassShape1, 0, 0);
        for (int j5 = 0; j5 < numOfMapMarkers; j5++) {
            int mapX = (markPosX[j5] * 4 + 2) - session_player.boundExtentX / 32;
            int mapY = (markPosY[j5] * 4 + 2) - session_player.boundExtentY / 32;
            markMinimap(markGraphic[j5], mapX, mapY);
        }

        for (int k5 = 0; k5 < 104; k5++) {
            for (int l5 = 0; l5 < 104; l5++) {
                Deque class19 = groundArray[plane][k5][l5];
                if (class19 != null) {
                    int l = (k5 * 4 + 2) - session_player.boundExtentX / 32;
                    int j3 = (l5 * 4 + 2) - session_player.boundExtentY / 32;
                    markMinimap(mapDotItem, l, j3);
                }
            }

        }

        for (int i6 = 0; i6 < sessionNpcCount; i6++) {
            Npc npc = sessionNpcs[sessionNpcList[i6]];
            if (npc != null && npc.isVisible()) {
                NpcDef npcDef = npc.desc;
                if (npcDef.childrenIDs != null)
                    npcDef = npcDef.method161();
                if (npcDef != null && npcDef.aBoolean87 && npcDef.aBoolean84) {
                    int i1 = npc.boundExtentX / 32 - session_player.boundExtentX / 32;
                    int k3 = npc.boundExtentY / 32 - session_player.boundExtentY / 32;
                    markMinimap(mapDotNPC, i1, k3);
                }
            }
        }

        for (int j6 = 0; j6 < session_player_count; j6++) {
            Player player = session_players[session_player_list[j6]];
            if (player != null && player.isVisible()) {
                int j1 = player.boundExtentX / 32 - session_player.boundExtentX / 32;
                int l3 = player.boundExtentY / 32 - session_player.boundExtentY / 32;
                boolean flag1 = false;
                long l6 = TextClass.longForName(player.name);
                for (int k6 = 0; k6 < user_friends_count; k6++) {
                    if (l6 != friendsListAsLongs[k6] || user_friends_worldid[k6] == 0)
                        continue;
                    flag1 = true;
                    break;
                }

                boolean flag2 = false;
                if (session_player.team != 0 && player.team != 0 && session_player.team == player.team)
                    flag2 = true;
                if (flag1)
                    markMinimap(mapDotFriend, j1, l3);
                else if (flag2)
                    markMinimap(mapDotTeam, j1, l3);
                else
                    markMinimap(mapDotPlayer, j1, l3);
            }
        }

        if (anInt855 != 0 && currentTime % 20 < 10) {
            if (anInt855 == 1 && anInt1222 >= 0 && anInt1222 < sessionNpcs.length) {
                Npc class30_sub2_sub4_sub1_sub1_1 = sessionNpcs[anInt1222];
                if (class30_sub2_sub4_sub1_sub1_1 != null) {
                    int k1 = class30_sub2_sub4_sub1_sub1_1.boundExtentX / 32 - session_player.boundExtentX / 32;
                    int i4 = class30_sub2_sub4_sub1_sub1_1.boundExtentY / 32 - session_player.boundExtentY / 32;
                    method81(mapMarker, i4, k1);
                }
            }
            if (anInt855 == 2) {
                int l1 = ((anInt934 - baseX) * 4 + 2) - session_player.boundExtentX / 32;
                int j4 = ((anInt935 - baseY) * 4 + 2) - session_player.boundExtentY / 32;
                method81(mapMarker, j4, l1);
            }
            if (anInt855 == 10 && anInt933 >= 0 && anInt933 < session_players.length) {
                Player class30_sub2_sub4_sub1_sub2_1 = session_players[anInt933];
                if (class30_sub2_sub4_sub1_sub2_1 != null) {
                    int i2 = class30_sub2_sub4_sub1_sub2_1.boundExtentX / 32 - session_player.boundExtentX / 32;
                    int k4 = class30_sub2_sub4_sub1_sub2_1.boundExtentY / 32 - session_player.boundExtentY / 32;
                    method81(mapMarker, k4, i2);
                }
            }
        }
        if (destX != 0) {
            int j2 = (destX * 4 + 2) - session_player.boundExtentX / 32;
            int l4 = (destY * 4 + 2) - session_player.boundExtentY / 32;
            markMinimap(mapFlag, j2, l4);
        }
        Graphics2D.fillRect(97, 78, 3, 3, 0xffffff);
        gameScreenCanvas.initDrawingArea();
    }

    private void npcScreenPos(Mobile mobile, int i) {
        calcEntityScreenPos(mobile.boundExtentX, i, mobile.boundExtentY);

        //aryan	mobile.entScreenX = spriteDrawX; mobile.entScreenY = spriteDrawY;
    }

    private void calcEntityScreenPos(int i, int j, int l) {
        if (i < 128 || l < 128 || i > 13056 || l > 13056) {
            spriteDrawX = -1;
            spriteDrawY = -1;
            return;
        }
        int i1 = method42(plane, l, i) - j;
        i -= xCameraPos;
        i1 -= zCameraPos;
        l -= yCameraPos;
        int j1 = Model.SINE[yCameraCurve];
        int k1 = Model.COSINE[yCameraCurve];
        int l1 = Model.SINE[xCameraCurve];
        int i2 = Model.COSINE[xCameraCurve];
        int j2 = l * l1 + i * i2 >> 16;
        l = l * i2 - i * l1 >> 16;
        i = j2;
        j2 = i1 * k1 - l * j1 >> 16;
        l = i1 * j1 + l * k1 >> 16;
        i1 = j2;
        if (l >= 50) {
            spriteDrawX = Rasterizer.centerX + (i << 9) / l;
            spriteDrawY = Rasterizer.centerY + (i1 << 9) / l;
        } else {
            spriteDrawX = -1;
            spriteDrawY = -1;
        }
    }

    private void buildSplitPrivateChatMenu() {
        if (ui_split_private_chat == 0)
            return;
        int i = 0;
        if (anInt1104 != 0)
            i = 1;
        for (int j = 0; j < 100; j++)
            if (chatMessages[j] != null) {
                int k = chatTypes[j];
                String s = chatNames[j];
                //boolean flag1 = false;//never used
                if (s != null && s.startsWith("@cr1@")) {
                    s = s.substring(5);
                    //boolean flag2 = true;//never used
                }
                if (s != null && s.startsWith("@cr2@")) {
                    s = s.substring(5);
                    //byte byte0 = 2;//never used
                }
                if ((k == 3 || k == 7) && (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
                    int l = 329 - i * 13;
                    if (super.mouseEventX > 4 && super.mouseEventY - 4 > l - 10 && super.mouseEventY - 4 <= l + 3) {
                        int i1 = plainFont.getTextWidth("From:  " + s + chatMessages[j]) + 25;
                        if (i1 > 450)
                            i1 = 450;
                        if (super.mouseEventX < 4 + i1) {
                            if (myPrivilege >= 1) {
                                menuActionName[menuActionRow] = "Report abuse @whi@" + s;
                                menuActionID[menuActionRow] = 2606;
                                menuActionRow++;
                            }
                            menuActionName[menuActionRow] = "Add ignore @whi@" + s;
                            menuActionID[menuActionRow] = 2042;
                            menuActionRow++;
                            menuActionName[menuActionRow] = "Add friend @whi@" + s;
                            menuActionID[menuActionRow] = 2337;
                            menuActionRow++;
                        }
                    }
                    if (++i >= 5)
                        return;
                }
                if ((k == 5 || k == 6) && privateChatMode < 2 && ++i >= 5)
                    return;
            }

    }

    private void createObjectSpawnRequest(int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) {
        GameObjectSpawnRequest gameObjectSpawnRequest = null;
        for (GameObjectSpawnRequest gameObjectSpawnRequest_1 = (GameObjectSpawnRequest) aClass19_1179.getFront(); gameObjectSpawnRequest_1 != null; gameObjectSpawnRequest_1 = (GameObjectSpawnRequest) aClass19_1179.getNext()) {
            if (gameObjectSpawnRequest_1.plane != l1 || gameObjectSpawnRequest_1.x != i2 || gameObjectSpawnRequest_1.z != j1 || gameObjectSpawnRequest_1.anInt1296 != i1)
                continue;
            gameObjectSpawnRequest = gameObjectSpawnRequest_1;
            break;
        }

        if (gameObjectSpawnRequest == null) {
            gameObjectSpawnRequest = new GameObjectSpawnRequest();
            gameObjectSpawnRequest.plane = l1;
            gameObjectSpawnRequest.anInt1296 = i1;
            gameObjectSpawnRequest.x = i2;
            gameObjectSpawnRequest.z = j1;
            method89(gameObjectSpawnRequest);
            aClass19_1179.insertBack(gameObjectSpawnRequest);
        }
        gameObjectSpawnRequest.id2 = k;
        gameObjectSpawnRequest.type2 = k1;
        gameObjectSpawnRequest.face2 = l;
        gameObjectSpawnRequest.anInt1302 = j2;
        gameObjectSpawnRequest.anInt1294 = j;
    }

    public PglWrapper getPglWrapper() {
        return pglWrapper;
    }

    private boolean interface_test_condition(RSInterface class9) {
        if (class9.condition_type == null)
            return false;
        for (int i = 0; i < class9.condition_type.length; i++) {
            int j = interface_calculate_dynamic_value(class9, i);
            int k = class9.condition_value_to_compare[i];
            if (class9.condition_type[i] == 2) {
                if (j >= k)
                    return false;
            } else if (class9.condition_type[i] == 3) {
                if (j <= k)
                    return false;
            } else if (class9.condition_type[i] == 4) {
                if (j == k)
                    return false;
            } else if (j != k)
                return false;
        }

        return true;
    }

    private DataInputStream openJagGrabInputStream(String s) throws IOException {
        //       if(!aBoolean872)
        //           if(Signlink.mainapp != null)
        //               return Signlink.openurl(s);
        //           else
        //               return new DataInputStream((new URL(getCodeBase(), s)).openStream());
        if (aSocket832 != null) {
            try {
                aSocket832.close();
            } catch (Exception _ex) {
            }
            aSocket832 = null;
        }
        aSocket832 = openSocket(43595);
        aSocket832.setSoTimeout(10000);
        java.io.InputStream inputstream = aSocket832.getInputStream();
        OutputStream outputstream = aSocket832.getOutputStream();
        outputstream.write(("JAGGRAB /" + s + "\n\n").getBytes());
        return new DataInputStream(inputstream);
    }

    private void doFlamesDrawing() {
        char c = '\u0100';
        if (anInt1040 > 0) {
            for (int i = 0; i < 256; i++)
                if (anInt1040 > 768)
                    anIntArray850[i] = rotateFlameColour(anIntArray851[i], anIntArray852[i], 1024 - anInt1040);
                else if (anInt1040 > 256)
                    anIntArray850[i] = anIntArray852[i];
                else
                    anIntArray850[i] = rotateFlameColour(anIntArray852[i], anIntArray851[i], 256 - anInt1040);

        } else if (anInt1041 > 0) {
            for (int j = 0; j < 256; j++)
                if (anInt1041 > 768)
                    anIntArray850[j] = rotateFlameColour(anIntArray851[j], anIntArray853[j], 1024 - anInt1041);
                else if (anInt1041 > 256)
                    anIntArray850[j] = anIntArray853[j];
                else
                    anIntArray850[j] = rotateFlameColour(anIntArray853[j], anIntArray851[j], 256 - anInt1041);

        } else {
            System.arraycopy(anIntArray851, 0, anIntArray850, 0, 256);

        }
        System.arraycopy(aClass30_Sub2_Sub1_Sub1_1201.myPixels, 0, aRSImageProducer_1110.componentPixels, 0, 33920);

        int i1 = 0;
        int j1 = 1152;
        for (int k1 = 1; k1 < c - 1; k1++) {
            int l1 = (anIntArray969[k1] * (c - k1)) / c;
            int j2 = 22 + l1;
            if (j2 < 0)
                j2 = 0;
            i1 += j2;
            for (int l2 = j2; l2 < 128; l2++) {
                int j3 = anIntArray828[i1++];
                if (j3 != 0) {
                    int l3 = j3;
                    int j4 = 256 - j3;
                    j3 = anIntArray850[j3];
                    int l4 = aRSImageProducer_1110.componentPixels[j1];
                    aRSImageProducer_1110.componentPixels[j1++] = ((j3 & 0xff00ff) * l3 + (l4 & 0xff00ff) * j4 & 0xff00ff00) + ((j3 & 0xff00) * l3 + (l4 & 0xff00) * j4 & 0xff0000) >> 8;
                } else {
                    j1++;
                }
            }

            j1 += j2;
        }

        aRSImageProducer_1110.drawGraphics(0, super.graphics, 0);
        System.arraycopy(aClass30_Sub2_Sub1_Sub1_1202.myPixels, 0, aRSImageProducer_1111.componentPixels, 0, 33920);

        i1 = 0;
        j1 = 1176;
        for (int k2 = 1; k2 < c - 1; k2++) {
            int i3 = (anIntArray969[k2] * (c - k2)) / c;
            int k3 = 103 - i3;
            j1 += i3;
            for (int i4 = 0; i4 < k3; i4++) {
                int k4 = anIntArray828[i1++];
                if (k4 != 0) {
                    int i5 = k4;
                    int j5 = 256 - k4;
                    k4 = anIntArray850[k4];
                    int k5 = aRSImageProducer_1111.componentPixels[j1];
                    aRSImageProducer_1111.componentPixels[j1++] = ((k4 & 0xff00ff) * i5 + (k5 & 0xff00ff) * j5 & 0xff00ff00) + ((k4 & 0xff00) * i5 + (k5 & 0xff00) * j5 & 0xff0000) >> 8;
                } else {
                    j1++;
                }
            }

            i1 += 128 - k3;
            j1 += 128 - k3 - i3;
        }

        aRSImageProducer_1111.drawGraphics(0, super.graphics, 637);
    }

    private void updatePlayer(Packet stream) {
        int j = stream.gbits(8);
        if (j < session_player_count) {
            for (int k = j; k < session_player_count; k++)
                anIntArray840[anInt839++] = session_player_list[k];

        }
        if (j > session_player_count) {
            Signlink.reporterror(myUsername + " Too many players");
            throw new RuntimeException("eek");
        }
        session_player_count = 0;
        for (int l = 0; l < j; l++) {
            int i1 = session_player_list[l];
            Player player = session_players[i1];
            int j1 = stream.gbits(1);
            if (j1 == 0) {
                session_player_list[session_player_count++] = i1;
                player.time = currentTime;
            } else {
                int k1 = stream.gbits(2);
                if (k1 == 0) {
                    session_player_list[session_player_count++] = i1;
                    player.time = currentTime;
                    session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = i1;
                } else if (k1 == 1) {
                    session_player_list[session_player_count++] = i1;
                    player.time = currentTime;
                    int l1 = stream.gbits(3);
                    player.move(false, l1);
                    int j2 = stream.gbits(1);
                    if (j2 == 1)
                        session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = i1;
                } else if (k1 == 2) {
                    session_player_list[session_player_count++] = i1;
                    player.time = currentTime;
                    int i2 = stream.gbits(3);
                    player.move(true, i2);
                    int k2 = stream.gbits(3);
                    player.move(true, k2);
                    int l2 = stream.gbits(1);
                    if (l2 == 1)
                        session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = i1;
                } else if (k1 == 3)
                    anIntArray840[anInt839++] = i1;
            }
        }
    }

    private void drawLoginScreen(boolean flag, int stage) {
        resetImageProducers();
        aRSImageProducer_1109.initDrawingArea();
        if (stage != 1) {
            titlebox.drawSprite(0, 0);
        } else {
            Graphics2D.resetImage();//need to redraw titlescreen (black space)
            drawLogo();
            aRSImageProducer_1109.initDrawingArea();
            loadingBox1.drawSprite(60, 20);
        }
        char c = '\u0168';//TODO change these to ints or something
        char c1 = '\310';
        if (loginScreenState == 0 && stage != 1) {
            int j = c1 / 2 - 40;
            if (super.mouseEventX >= 291 && super.mouseEventX <= 467 && super.mouseEventY >= 333 && super.mouseEventY <= 355)
                loginhover.drawSprite(88, 161); // click here to play
            if (super.mouseEventX >= 273 && super.mouseEventX <= 486 && super.mouseEventY >= 276 && super.mouseEventY <= 300)
                passwordhover.drawSprite(68, 104); // pass hover
            if (super.mouseEventX >= 273 && super.mouseEventX <= 486 && super.mouseEventY >= 230 && super.mouseEventY <= 254)
                passwordhover.drawSprite(68, 58); // user hover
            if (loginMessage1.length() > 0) {
                //loadingBox1.drawSprite(0, 0);
                plainFont.method382(0xffff00, c / 2, loginMessage1, j - 6, true);
                plainFont.method382(0xffff00, c / 2, loginMessage2, j - 6, true);
                j += 30;
            } else {
                //loadingBox1.drawSprite(0, 0);
                boldFont.method382(0xffff00, c / 2, loginMessage2, j - 6, true);
                j += 30;
            }
            plainFont.method389(true, c / 2 - 105, 0xffffff, myUsername + ((loginScreenCursorPos == 0) & (currentTime % 40 < 20) ? "@whi@|" : ""), j - 12);
            j += 15;
            plainFont.method389(true, c / 2 - 105, 0xffffff, TextClass.passwordAsterisks(myPassword) + ((loginScreenCursorPos == 1) & (currentTime % 40 < 30) ? "@whi@|" : ""), j + 18);
            j += 15;
            if (!flag) {
                //int i1 = c / 2 - 80;//never used
                //int l1 = c1 / 2 + 50;//never used
            }
        }

        aRSImageProducer_1109.drawGraphics(171, super.graphics, 202);
        if (repaintRequested) {
            repaintRequested = false;
            aRSImageProducer_1107.drawGraphics(0, super.graphics, 128);
            aRSImageProducer_1108.drawGraphics(371, super.graphics, 202);
            aRSImageProducer_1112.drawGraphics(265, super.graphics, 0);
            aRSImageProducer_1113.drawGraphics(265, super.graphics, 562);
            aRSImageProducer_1114.drawGraphics(171, super.graphics, 128);
            aRSImageProducer_1115.drawGraphics(171, super.graphics, 562);
        }
    }

    private void drawFlames() {
        drawingFlames = true;
        try {
            long l = System.currentTimeMillis();
            int i = 0;
            int j = 20;
            while (aBoolean831) {
                anInt1208++;
                //calcFlamesPosition();
                //calcFlamesPosition();
                doFlamesDrawing();
                if (++i > 10) {
                    long l1 = System.currentTimeMillis();
                    int k = (int) (l1 - l) / 10 - j;
                    j = 40 - k;
                    if (j < 5)
                        j = 5;
                    i = 0;
                    l = l1;
                }
                try {
                    Thread.sleep(j);
                } catch (Exception _ex) {
                }
            }
        } catch (Exception _ex) {
        }
        drawingFlames = false;
    }

    public void repaintGame() {
        repaintRequested = true;
    }

    private void method137(Packet stream, int j) {
        if (j == 84) {
            int k = stream.g1();
            int j3 = anInt1268 + (k >> 4 & 7);
            int i6 = anInt1269 + (k & 7);
            int l8 = stream.g2();
            int k11 = stream.g2();
            int l13 = stream.g2();
            if (j3 >= 0 && i6 >= 0 && j3 < 104 && i6 < 104) {
                Deque class19_1 = groundArray[plane][j3][i6];
                if (class19_1 != null) {
                    for (Item class30_sub2_sub4_sub2_3 = (Item) class19_1.getFront(); class30_sub2_sub4_sub2_3 != null; class30_sub2_sub4_sub2_3 = (Item) class19_1.getNext()) {
                        if (class30_sub2_sub4_sub2_3.ID != (l8 & 0x7fff) || class30_sub2_sub4_sub2_3.anInt1559 != k11)
                            continue;
                        class30_sub2_sub4_sub2_3.anInt1559 = l13;
                        break;
                    }

                    spawnGroundItem(j3, i6);
                }
            }
            return;
        }
        if (j == 105) {
            int l = stream.g1();
            int k3 = anInt1268 + (l >> 4 & 7);
            int j6 = anInt1269 + (l & 7);
            int i9 = stream.g2();
            int l11 = stream.g1();
            int i14 = l11 >> 4 & 0xf;
            int i16 = l11 & 7;
            if (session_player.pathX[0] >= k3 - i14 && session_player.pathX[0] <= k3 + i14 && session_player.pathY[0] >= j6 - i14 && session_player.pathY[0] <= j6 + i14 && wave_on && !lowMem && anInt1062 < 50) {
                anIntArray1207[anInt1062] = i9;
                anIntArray1241[anInt1062] = i16;
                anIntArray1250[anInt1062] = Sound.anIntArray326[i9];
                anInt1062++;
            }
        }
        if (j == 215) {
            int i1 = stream.sg2();
            int l3 = stream.sg1();
            int k6 = anInt1268 + (l3 >> 4 & 7);
            int j9 = anInt1269 + (l3 & 7);
            int i12 = stream.sg2();
            int j14 = stream.g2();
            if (k6 >= 0 && j9 >= 0 && k6 < 104 && j9 < 104 && i12 != unknownInt10) {
                Item class30_sub2_sub4_sub2_2 = new Item();
                class30_sub2_sub4_sub2_2.ID = i1;
                class30_sub2_sub4_sub2_2.anInt1559 = j14;
                if (groundArray[plane][k6][j9] == null)
                    groundArray[plane][k6][j9] = new Deque();
                groundArray[plane][k6][j9].insertBack(class30_sub2_sub4_sub2_2);
                spawnGroundItem(k6, j9);
            }
            return;
        }
        if (j == 156) {
            int j1 = stream.nsp1();
            int i4 = anInt1268 + (j1 >> 4 & 7);
            int l6 = anInt1269 + (j1 & 7);
            int k9 = stream.g2();
            if (i4 >= 0 && l6 >= 0 && i4 < 104 && l6 < 104) {
                Deque class19 = groundArray[plane][i4][l6];
                if (class19 != null) {
                    for (Item item = (Item) class19.getFront(); item != null; item = (Item) class19.getNext()) {
                        if (item.ID != (k9 & 0x7fff))
                            continue;
                        item.unlink();
                        break;
                    }

                    if (class19.getFront() == null)
                        groundArray[plane][i4][l6] = null;
                    spawnGroundItem(i4, l6);
                }
            }
            return;
        }
        if (j == 160) {
            int k1 = stream.sg1();
            int x = anInt1268 + (k1 >> 4 & 7);
            int y = anInt1269 + (k1 & 7);
            int l9 = stream.sg1();
            int j12 = l9 >> 2;
            int k14 = l9 & 3;
            int j16 = anIntArray1177[j12];
            int j17 = stream.sg2();
            if (x >= 0 && y >= 0 && x < 103 && y < 103) {
                int j18 = intGroundArray[plane][x][y];
                int i19 = intGroundArray[plane][x + 1][y];
                int l19 = intGroundArray[plane][x + 1][y + 1];
                int k20 = intGroundArray[plane][x][y + 1];
                if (j16 == 0) {
                    WallObject wallObject = sceneGraph.getWallObject(plane, x, y);
                    if (wallObject != null) {
                        int k21 = wallObject.uid >> 14 & 0x7fff;
                        if (j12 == 2) {
                            wallObject.node1 = new ObjectOnTile(k21, 4 + k14, 2, i19, l19, j18, k20, j17, false);
                            wallObject.node2 = new ObjectOnTile(k21, k14 + 1 & 3, 2, i19, l19, j18, k20, j17, false);
                        } else {
                            wallObject.node1 = new ObjectOnTile(k21, k14, j12, i19, l19, j18, k20, j17, false);
                        }
                    }
                }
                if (j16 == 1) {
                    WallDecoration wallDecoration = sceneGraph.getWallDecoration(x, y, plane);
                    if (wallDecoration != null)
                        wallDecoration.myMob = new ObjectOnTile(wallDecoration.uid >> 14 & 0x7fff, 0, 4, i19, l19, j18, k20, j17, false);
                }
                if (j16 == 2) {
                    InteractableObject class28 = sceneGraph.getInteractableObject(x, y, plane);
                    if (j12 == 11)
                        j12 = 10;
                    if (class28 != null)
                        class28.jagexNode = new ObjectOnTile(class28.uid >> 14 & 0x7fff, k14, j12, i19, l19, j18, k20, j17, false);
                }
                if (j16 == 3) {
                    GroundDecoration class49 = sceneGraph.getGroundDecoration(y, x, plane);
                    if (class49 != null)
                        class49.aClass30_Sub2_Sub4_814 = new ObjectOnTile(class49.uid >> 14 & 0x7fff, k14, 22, i19, l19, j18, k20, j17, false);
                }
            }
            return;
        }
        if (j == 147) {
            int l1 = stream.sg1();
            int k4 = anInt1268 + (l1 >> 4 & 7);
            int j7 = anInt1269 + (l1 & 7);
            int i10 = stream.g2();
            byte byte0 = stream.sg1b();
            int l14 = stream.ig2();
            byte byte1 = stream.ng1();
            int k17 = stream.g2();
            int k18 = stream.sg1();
            int j19 = k18 >> 2;
            int i20 = k18 & 3;
            int l20 = anIntArray1177[j19];
            byte byte2 = stream.g1b();
            int l21 = stream.g2();
            byte byte3 = stream.ng1();
            Player player;
            if (i10 == unknownInt10)
                player = session_player;
            else
                player = session_players[i10];
            if (player != null) {
                ObjectDef class46 = ObjectDef.forID(l21);
                int i22 = intGroundArray[plane][k4][j7];
                int j22 = intGroundArray[plane][k4 + 1][j7];
                int k22 = intGroundArray[plane][k4 + 1][j7 + 1];
                int l22 = intGroundArray[plane][k4][j7 + 1];
                Model model = class46.method578(j19, i20, i22, j22, k22, l22, -1);
                if (model != null) {
                    createObjectSpawnRequest(k17 + 1, -1, 0, l20, j7, 0, plane, k4, l14 + 1);
                    player.anInt1707 = l14 + currentTime;
                    player.anInt1708 = k17 + currentTime;
                    player.aModel_1714 = model;
                    int i23 = class46.sizeX;
                    int j23 = class46.sizeY;
                    if (i20 == 1 || i20 == 3) {
                        i23 = class46.sizeY;
                        j23 = class46.sizeX;
                    }
                    player.anInt1711 = k4 * 128 + i23 * 64;
                    player.anInt1713 = j7 * 128 + j23 * 64;
                    player.anInt1712 = method42(plane, player.anInt1713, player.anInt1711);
                    if (byte2 > byte0) {
                        byte byte4 = byte2;
                        byte2 = byte0;
                        byte0 = byte4;
                    }
                    if (byte3 > byte1) {
                        byte byte5 = byte3;
                        byte3 = byte1;
                        byte1 = byte5;
                    }
                    player.anInt1719 = k4 + byte2;
                    player.anInt1721 = k4 + byte0;
                    player.anInt1720 = j7 + byte3;
                    player.anInt1722 = j7 + byte1;
                }
            }
        }
        if (j == 151) {
            int i2 = stream.nsp1();
            int l4 = anInt1268 + (i2 >> 4 & 7);
            int k7 = anInt1269 + (i2 & 7);
            int j10 = stream.ig2();
            int k12 = stream.sg1();
            int i15 = k12 >> 2;
            int k16 = k12 & 3;
            int l17 = anIntArray1177[i15];
            if (l4 >= 0 && k7 >= 0 && l4 < 104 && k7 < 104)
                createObjectSpawnRequest(-1, j10, k16, l17, k7, i15, plane, l4, 0);
            return;
        }
        if (j == 4) {
            int j2 = stream.g1();
            int i5 = anInt1268 + (j2 >> 4 & 7);
            int l7 = anInt1269 + (j2 & 7);
            int k10 = stream.g2();
            int l12 = stream.g1();
            int j15 = stream.g2();
            if (i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104) {
                i5 = i5 * 128 + 64;
                l7 = l7 * 128 + 64;
                StillGraphic class30_sub2_sub4_sub3 = new StillGraphic(plane, currentTime, j15, k10, method42(plane, l7, i5) - l12, l7, i5);
                aClass19_1056.insertBack(class30_sub2_sub4_sub3);
            }
            return;
        }
        if (j == 44) {
            int k2 = stream.isg2();
            int j5 = stream.g2();
            int i8 = stream.g1();
            int l10 = anInt1268 + (i8 >> 4 & 7);
            int i13 = anInt1269 + (i8 & 7);
            if (l10 >= 0 && i13 >= 0 && l10 < 104 && i13 < 104) {
                Item class30_sub2_sub4_sub2_1 = new Item();
                class30_sub2_sub4_sub2_1.ID = k2;
                class30_sub2_sub4_sub2_1.anInt1559 = j5;
                if (groundArray[plane][l10][i13] == null)
                    groundArray[plane][l10][i13] = new Deque();
                groundArray[plane][l10][i13].insertBack(class30_sub2_sub4_sub2_1);
                spawnGroundItem(l10, i13);
            }
            return;
        }
        if (j == 101) {
            int l2 = stream.ng1b();
            int k5 = l2 >> 2;
            int j8 = l2 & 3;
            int i11 = anIntArray1177[k5];
            int j13 = stream.g1();
            int k15 = anInt1268 + (j13 >> 4 & 7);
            int l16 = anInt1269 + (j13 & 7);
            if (k15 >= 0 && l16 >= 0 && k15 < 104 && l16 < 104)
                createObjectSpawnRequest(-1, -1, j8, i11, l16, k5, plane, k15, 0);
            return;
        }
        if (j == 117) {
            int i3 = stream.g1();
            int l5 = anInt1268 + (i3 >> 4 & 7);
            int k8 = anInt1269 + (i3 & 7);
            int j11 = l5 + stream.g1b();
            int k13 = k8 + stream.g1b();
            int l15 = stream.g2b();
            int i17 = stream.g2();
            int i18 = stream.g1() * 4;
            int l18 = stream.g1() * 4;
            int k19 = stream.g2();
            int j20 = stream.g2();
            int i21 = stream.g1();
            int j21 = stream.g1();
            if (l5 >= 0 && k8 >= 0 && l5 < 104 && k8 < 104 && j11 >= 0 && k13 >= 0 && j11 < 104 && k13 < 104 && i17 != 65535) {
                l5 = l5 * 128 + 64;
                k8 = k8 * 128 + 64;
                j11 = j11 * 128 + 64;
                k13 = k13 * 128 + 64;
                Projectile class30_sub2_sub4_sub4 = new Projectile(i21, l18, k19 + currentTime, j20 + currentTime, j21, plane, method42(plane, k8, l5) - i18, k8, l5, l15, i17);
                class30_sub2_sub4_sub4.method455(k19 + currentTime, k13, method42(plane, k13, j11) - l18, j11);
                aClass19_1013.insertBack(class30_sub2_sub4_sub4);
            }
        }
    }


    private static void setLowMem() {
        SceneGraph.lowMem = true;
        Rasterizer.lowMem = true;
        lowMem = true;
        MapRegion.lowMem = true;
        ObjectDef.lowMem = true;
    }

    private void updateNPCs(Packet stream) {
        stream.begin_bit_block();
        int k = stream.gbits(8);
        if (k < sessionNpcCount) {
            for (int l = k; l < sessionNpcCount; l++)
                anIntArray840[anInt839++] = sessionNpcList[l];

        }
        if (k > sessionNpcCount) {
            Signlink.reporterror(myUsername + " Too many npcs");
            throw new RuntimeException("eek");
        }
        sessionNpcCount = 0;
        for (int i1 = 0; i1 < k; i1++) {
            int j1 = sessionNpcList[i1];
            Npc npc = sessionNpcs[j1];
            int k1 = stream.gbits(1);
            if (k1 == 0) {
                sessionNpcList[sessionNpcCount++] = j1;
                npc.time = currentTime;
            } else {
                int l1 = stream.gbits(2);
                if (l1 == 0) {
                    sessionNpcList[sessionNpcCount++] = j1;
                    npc.time = currentTime;
                    session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = j1;
                } else if (l1 == 1) {
                    sessionNpcList[sessionNpcCount++] = j1;
                    npc.time = currentTime;
                    int i2 = stream.gbits(3);
                    npc.move(false, i2);
                    int k2 = stream.gbits(1);
                    if (k2 == 1)
                        session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = j1;
                } else if (l1 == 2) {
                    sessionNpcList[sessionNpcCount++] = j1;
                    npc.time = currentTime;
                    int j2 = stream.gbits(3);
                    npc.move(true, j2);
                    int l2 = stream.gbits(3);
                    npc.move(true, l2);
                    int i3 = stream.gbits(1);
                    if (i3 == 1)
                        session_npcs_awaiting_update[sessionNpcsAwaitingUpdatePtr++] = j1;
                } else if (l1 == 3)
                    anIntArray840[anInt839++] = j1;
            }
        }

    }

    private void login_screen_process() {
        if (loginScreenState == 0) {
            if (super.mouseButtonPressed == 1 && super.clickX >= 273 && super.clickX <= 486 && super.clickY >= 276 && super.clickY <= 300)
                loginScreenCursorPos = 1;
            if (super.mouseButtonPressed == 1 && super.clickX >= 273 && super.clickX <= 486 && super.clickY >= 230 && super.clickY <= 254)
                loginScreenCursorPos = 0;

            if (super.mouseButtonPressed == 1 && super.clickX >= 291 && super.clickX <= 467 && super.clickY >= 333 && super.clickY <= 355) {
                if (myUsername.length() < 1) {
                    loginMessage2 = "Please enter a username!";
                    loginScreenCursorPos = 0;
                } else if (myPassword.length() < 1) {
                    loginMessage2 = "Please enter a password!";
                    loginScreenCursorPos = 1;
                } else {
                    loginFailures = 0;
                    login(myUsername, myPassword, false);
                    if (loggedIn)
                        return;
                }
            }

            do {
                int l1 = readChar();
                if (l1 == -1)
                    break;
                boolean flag1 = false;
                for (int i2 = 0; i2 < validUserPassChars.length(); i2++) {
                    if (l1 != validUserPassChars.charAt(i2))
                        continue;
                    flag1 = true;
                    break;
                }

                if (loginScreenCursorPos == 0) {
                    if (l1 == 8 && myUsername.length() > 0)
                        myUsername = myUsername.substring(0, myUsername.length() - 1);
                    if (l1 == 9 || l1 == 10 || l1 == 13)
                        loginScreenCursorPos = 1;
                    if (flag1)
                        myUsername += (char) l1;
                    if (myUsername.length() > 12)
                        myUsername = myUsername.substring(0, 12);
                } else if (loginScreenCursorPos == 1) {
                    if (l1 == 8 && myPassword.length() > 0)
                        myPassword = myPassword.substring(0, myPassword.length() - 1);
                    if (l1 == 9 || l1 == 10 || l1 == 13)
                        loginScreenCursorPos = 0;
                    if (flag1)
                        myPassword += (char) l1;
                    if (myPassword.length() > 20)
                        myPassword = myPassword.substring(0, 20);
                }
            } while (true);
            return;
        }

    }

    private void markMinimap(RgbImage rgbImage, int i, int j) {
        int k = minimapInt1 + minimapInt2 & 0x7ff;
        int l = i * i + j * j;
        if (l > 6400)
            return;
        int i1 = Model.SINE[k];
        int j1 = Model.COSINE[k];
        i1 = (i1 * 256) / (minimapInt3 + 256);
        j1 = (j1 * 256) / (minimapInt3 + 256);
        int k1 = j * i1 + i * j1 >> 16;
        int l1 = j * j1 - i * i1 >> 16;
        if (l > 2500) {
            rgbImage.method354(mapBack, 83 - l1 - rgbImage.h2 / 2 - 4, ((94 + k1) - rgbImage.w2 / 2) + 4);
        } else {
            rgbImage.drawSprite(((94 + k1) - rgbImage.w2 / 2) + 4, 83 - l1 - rgbImage.h2 / 2 - 4);
        }
    }

    private void method142(int y, int z, int k, int l, int x, int j1, int k1) {
        if (x >= 1 && y >= 1 && x <= 102 && y <= 102) {
            if (lowMem && z != plane)
                return;
            int i2 = 0;
            if (j1 == 0)
                i2 = sceneGraph.getWallObjectUID(z, x, y);
            if (j1 == 1)
                i2 = sceneGraph.getWallDecorationUID(z, x, y);
            if (j1 == 2)
                i2 = sceneGraph.getInteractableObjectUID(z, x, y);
            if (j1 == 3)
                i2 = sceneGraph.getGroundDecorationUID(z, x, y);
            if (i2 != 0) {
                int i3 = sceneGraph.getIDTAGForXYZ(z, x, y, i2);
                int j2 = i2 >> 14 & 0x7fff;
                int k2 = i3 & 0x1f;
                int l2 = i3 >> 6;
                if (j1 == 0) {
                    sceneGraph.removeWallObject(x, z, y);
                    ObjectDef class46 = ObjectDef.forID(j2);
                    if (class46.isUnwalkable)
                        tileSettings[z].method215(l2, k2, class46.aBoolean757, x, y);
                }
                if (j1 == 1)
                    sceneGraph.removeWallDecoration(y, z, x);
                if (j1 == 2) {
                    sceneGraph.method293(z, x, y);
                    ObjectDef class46_1 = ObjectDef.forID(j2);
                    if (x + class46_1.sizeX > 103 || y + class46_1.sizeX > 103 || x + class46_1.sizeY > 103 || y + class46_1.sizeY > 103)
                        return;
                    if (class46_1.isUnwalkable)
                        tileSettings[z].method216(l2, class46_1.sizeX, x, y, class46_1.sizeY, class46_1.aBoolean757);
                }
                if (j1 == 3) {
                    sceneGraph.removeGroundDecoration(z, y, x);
                    ObjectDef class46_2 = ObjectDef.forID(j2);
                    if (class46_2.isUnwalkable && class46_2.hasActions)
                        tileSettings[z].method218(y, x);
                }
            }
            if (k1 >= 0) {
                int j3 = z;
                if (j3 < 3 && (tileSettingBits[1][x][y] & 2) == 2)
                    j3++;
                MapRegion.method188(sceneGraph, k, y, l, j3, tileSettings[z], intGroundArray, x, k1, z);
            }
        }
    }

    private void updatePlayers(int i, Packet stream) {
        anInt839 = 0;
        sessionNpcsAwaitingUpdatePtr = 0;
        updatePlayerMovement(stream);
        updatePlayer(stream);
        updateOtherPlayerMovements(stream, i);
        refreshUpdateMasks(stream);
        for (int k = 0; k < anInt839; k++) {
            int l = anIntArray840[k];
            if (session_players[l].time != currentTime)
                session_players[l] = null;
        }

        if (stream.pos != i) {
            Signlink.reporterror("Error packet size mismatch in getplayer pos:" + stream.pos + " psize:" + i);
            throw new RuntimeException("eek");
        }
        for (int i1 = 0; i1 < session_player_count; i1++)
            if (session_players[session_player_list[i1]] == null) {
                Signlink.reporterror(myUsername + " null entry in pl list - pos:" + i1 + " size:" + session_player_count);
                throw new RuntimeException("eek");
            }

    }

    private void setCameraPos(int j, int k, int l, int i1, int j1, int k1) {
        int l1 = 2048 - k & 0x7ff;
        int i2 = 2048 - j1 & 0x7ff;
        int j2 = 0;
        int k2 = 0;
        int l2 = j;
        if (l1 != 0) {
            int i3 = Model.SINE[l1];
            int k3 = Model.COSINE[l1];
            int i4 = k2 * k3 - l2 * i3 >> 16;
            l2 = k2 * i3 + l2 * k3 >> 16;
            k2 = i4;
        }
        if (i2 != 0) {
            /* xxx            if(cameratoggle){
				if(zoom == 0)
				zoom = k2;
			if(lftrit == 0)
				lftrit = j2;
			if(fwdbwd == 0)
				fwdbwd = l2;
			k2 = zoom;
			j2 = lftrit;
			l2 = fwdbwd;
			}
*/
            int j3 = Model.SINE[i2];
            int l3 = Model.COSINE[i2];
            int j4 = l2 * j3 + j2 * l3 >> 16;
            l2 = l2 * l3 - j2 * j3 >> 16;
            j2 = j4;
        }
        xCameraPos = l - j2;
        zCameraPos = i1 - k2;
        yCameraPos = k1 - l2;
        yCameraCurve = k;
        xCameraCurve = j1;
    }

    private boolean parsePacket() {
        if (socketStream == null)
            return false;
        try {
            int i = socketStream.available();
            if (i == 0)
                return false;
            if (pktType == -1) {
                socketStream.flushInputStream(inStream.data, 1);
                pktType = inStream.data[0] & 0xff;
                if (encryption != null)
                    pktType = pktType - encryption.next() & 0xff;
                pktSize = PacketSizes.packetSizes[pktType];
                i--;
            }
            if (pktSize == -1)
                if (i > 0) {
                    socketStream.flushInputStream(inStream.data, 1);
                    pktSize = inStream.data[0] & 0xff;
                    i--;
                } else {
                    return false;
                }
            if (pktSize == -2)
                if (i > 1) {
                    socketStream.flushInputStream(inStream.data, 2);
                    inStream.pos = 0;
                    pktSize = inStream.g2();
                    i -= 2;
                } else {
                    return false;
                }
            if (i < pktSize)
                return false;
            inStream.pos = 0;
            socketStream.flushInputStream(inStream.data, pktSize);
            anInt1009 = 0;
            anInt843 = anInt842;
            anInt842 = anInt841;
            anInt841 = pktType;
            if (pktType == 81) {
                updatePlayers(pktSize, inStream);
                aBoolean1080 = false;
                pktType = -1;
                return true;
            }
            if (pktType == 176) {
                welcome_screen_last_recovery_change_or_member_warning = inStream.ng1b();
                welcome_screen_unread_pm_count = inStream.sg2();
                membersInt = inStream.g1();
                anInt1193 = inStream.biig4();
                welcome_screen_last_login = inStream.g2();
                if (anInt1193 != 0 && openInterfaceID == -1) {
                    Signlink.dnslookup(TextClass.decodeDNS(anInt1193));
                    clearTopInterfaces();
                    char c = '\u028A';
                    if (welcome_screen_last_recovery_change_or_member_warning != 201 || membersInt == 1)
                        c = '\u028F';
                    reportAbuseInput = "";
                    report_abuse_mute_player = false;
                    for (int k9 = 0; k9 < RSInterface.interfaceCache.length; k9++) {
                        if (RSInterface.interfaceCache[k9] == null || RSInterface.interfaceCache[k9].content_type != c)
                            continue;
                        openInterfaceID = RSInterface.interfaceCache[k9].parentID;
                        break;
                    }

                }
                pktType = -1;
                return true;
            }
            if (pktType == 64) {
                anInt1268 = inStream.ng1b();
                anInt1269 = inStream.sg1();
                for (int j = anInt1268; j < anInt1268 + 8; j++) {
                    for (int l9 = anInt1269; l9 < anInt1269 + 8; l9++)
                        if (groundArray[plane][j][l9] != null) {
                            groundArray[plane][j][l9] = null;
                            spawnGroundItem(j, l9);
                        }

                }

                for (GameObjectSpawnRequest gameObjectSpawnRequest = (GameObjectSpawnRequest) aClass19_1179.getFront(); gameObjectSpawnRequest != null; gameObjectSpawnRequest = (GameObjectSpawnRequest) aClass19_1179.getNext())
                    if (gameObjectSpawnRequest.x >= anInt1268 && gameObjectSpawnRequest.x < anInt1268 + 8 && gameObjectSpawnRequest.z >= anInt1269 && gameObjectSpawnRequest.z < anInt1269 + 8 && gameObjectSpawnRequest.plane == plane)
                        gameObjectSpawnRequest.anInt1294 = 0;

                pktType = -1;
                return true;
            }
            if (pktType == 185) {
                int k = inStream.isg2();
                RSInterface.interfaceCache[k].anInt233 = 3;
                if (session_player.desc == null)
                    RSInterface.interfaceCache[k].mediaID = (session_player.appearanceColours[0] << 25) + (session_player.appearanceColours[4] << 20) + (session_player.appearanceModels[0] << 15) + (session_player.appearanceModels[8] << 10) + (session_player.appearanceModels[11] << 5) + session_player.appearanceModels[1];
                else
                    RSInterface.interfaceCache[k].mediaID = (int) (0x12345678L + session_player.desc.type);
                pktType = -1;
                return true;
            }
            if (pktType == 107) {
                aBoolean1160 = false;
                for (int l = 0; l < 5; l++)
                    aBooleanArray876[l] = false;

                pktType = -1;
                return true;
            }
            if (pktType == 72) {
                int i1 = inStream.ig2();
                RSInterface class9 = RSInterface.interfaceCache[i1];
                for (int k15 = 0; k15 < class9.inv.length; k15++) {
                    class9.inv[k15] = -1;
                    class9.inv[k15] = 0;
                }

                pktType = -1;
                return true;
            }
            if (pktType == 214) {
                user_ignore_count = pktSize / 8;
                for (int j1 = 0; j1 < user_ignore_count; j1++)
                    user_ignore_names[j1] = inStream.g8();

                pktType = -1;
                return true;
            }
            if (pktType == 166) {
                aBoolean1160 = true;
                anInt1098 = inStream.g1();
                anInt1099 = inStream.g1();
                anInt1100 = inStream.g2();
                anInt1101 = inStream.g1();
                anInt1102 = inStream.g1();
                if (anInt1102 >= 100) {
                    xCameraPos = anInt1098 * 128 + 64;
                    yCameraPos = anInt1099 * 128 + 64;
                    zCameraPos = method42(plane, yCameraPos, xCameraPos) - anInt1100;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 134) {
                needDrawTabArea = true;
                int k1 = inStream.g1();
                int i10 = inStream.big4();
                int l15 = inStream.g1();
                user_experience[k1] = i10;
                user_levels[k1] = l15;
                user_stats[k1] = 1;
                for (int k20 = 0; k20 < 98; k20++)
                    if (i10 >= XP_FOR_LEVEL[k20])
                        user_stats[k1] = k20 + 2;

                pktType = -1;
                return true;
            }
            if (pktType == 71) {
                int l1 = inStream.g2();
                int j10 = inStream.nsp1();
                if (l1 == 65535)
                    l1 = -1;
                tabInterfaceIDs[j10] = l1;
                needDrawTabArea = true;
                tabAreaAltered = true;
                pktType = -1;
                return true;
            }
            if (pktType == 74) {
                int i2 = inStream.ig2();
                if (i2 == 65535)
                    i2 = -1;
                if (i2 != currentSong && musicEnabled && !lowMem && prevSong == 0) {
                    nextSong = i2;
                    songChanging = true;
                    onDemandFetcher.method558(2, nextSong);
                }
                currentSong = i2;
                pktType = -1;
                return true;
            }
            if (pktType == 121) {
                int j2 = inStream.isg2();
                int k10 = inStream.sg2();
                if (musicEnabled && !lowMem) {
                    nextSong = j2;
                    songChanging = false;
                    onDemandFetcher.method558(2, nextSong);
                    prevSong = k10;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 109) {
                network_disconnect();
                pktType = -1;
                return false;
            }
            if (pktType == 70) {
                int k2 = inStream.g2b();
                int l10 = inStream.ig2b();
                int i16 = inStream.ig2();
                RSInterface class9_5 = RSInterface.interfaceCache[i16];
                class9_5.x_offset = k2;
                class9_5.y_offset = l10;
                pktType = -1;
                return true;
            }
            if (pktType == 73 || pktType == 241) {

                //mapReset();
                int l2 = anInt1069;
                int i11 = anInt1070;
                if (pktType == 73) {
                    l2 = inStream.sg2();
                    i11 = inStream.g2();
                    aBoolean1159 = false;
                }
                if (pktType == 241) {
                    i11 = inStream.sg2();
                    inStream.begin_bit_block();
                    for (int j16 = 0; j16 < 4; j16++) {
                        for (int l20 = 0; l20 < 13; l20++) {
                            for (int j23 = 0; j23 < 13; j23++) {
                                int i26 = inStream.gbits(1);
                                if (i26 == 1)
                                    anIntArrayArrayArray1129[j16][l20][j23] = inStream.gbits(26);
                                else
                                    anIntArrayArrayArray1129[j16][l20][j23] = -1;
                            }

                        }

                    }

                    inStream.end_bit_block();
                    l2 = inStream.g2();
                    aBoolean1159 = true;
                }
                if (anInt1069 == l2 && anInt1070 == i11 && loadingStage == 2) {
                    pktType = -1;
                    return true;
                }
                anInt1069 = l2;
                anInt1070 = i11;
                baseX = (anInt1069 - 6) * 8;
                baseY = (anInt1070 - 6) * 8;
                aBoolean1141 = (anInt1069 / 8 == 48 || anInt1069 / 8 == 49) && anInt1070 / 8 == 48;
                if (anInt1069 / 8 == 48 && anInt1070 / 8 == 148)
                    aBoolean1141 = true;
                loadingStage = 1;
                aLong824 = System.currentTimeMillis();
                gameScreenCanvas.initDrawingArea();
                plainFont.drawText(0, "Loading - please wait.", 151, 257);
                plainFont.drawText(0xffffff, "Loading - please wait.", 150, 256);
                gameScreenCanvas.drawGraphics(4, super.graphics, 4);
                if (pktType == 73) {
                    int k16 = 0;
                    for (int i21 = (anInt1069 - 6) / 8; i21 <= (anInt1069 + 6) / 8; i21++) {
                        for (int k23 = (anInt1070 - 6) / 8; k23 <= (anInt1070 + 6) / 8; k23++)
                            k16++;

                    }

                    terrainData = new byte[k16][];
                    aByteArrayArray1247 = new byte[k16][];
                    mapCoordinates = new int[k16];
                    terrainIndices = new int[k16];
                    anIntArray1236 = new int[k16];
                    k16 = 0;
                    for (int mapx = (anInt1069 - 6) / 8; mapx <= (anInt1069 + 6) / 8; mapx++) {
                        for (int j26 = (anInt1070 - 6) / 8; j26 <= (anInt1070 + 6) / 8; j26++) {
                            mapCoordinates[k16] = (mapx << 8) + j26;
                            if (aBoolean1141 && (j26 == 49 || j26 == 149 || j26 == 147 || mapx == 50 || mapx == 49 && j26 == 47)) {
                                terrainIndices[k16] = -1;
                                anIntArray1236[k16] = -1;
                                k16++;
                            } else {
                                int k28 = terrainIndices[k16] = onDemandFetcher.getMapIndex(0, j26, mapx);
                                if (k28 != -1)
                                    onDemandFetcher.method558(3, k28);
                                int j30 = anIntArray1236[k16] = onDemandFetcher.getMapIndex(1, j26, mapx);
                                if (j30 != -1)
                                    onDemandFetcher.method558(3, j30);
                                k16++;
                            }
                        }

                    }

                }
                if (pktType == 241) {
                    int l16 = 0;
                    int ai[] = new int[676];
                    for (int i24 = 0; i24 < 4; i24++) {
                        for (int k26 = 0; k26 < 13; k26++) {
                            for (int l28 = 0; l28 < 13; l28++) {
                                int k30 = anIntArrayArrayArray1129[i24][k26][l28];
                                if (k30 != -1) {
                                    int k31 = k30 >> 14 & 0x3ff;
                                    int i32 = k30 >> 3 & 0x7ff;
                                    int k32 = (k31 / 8 << 8) + i32 / 8;
                                    for (int j33 = 0; j33 < l16; j33++) {
                                        if (ai[j33] != k32)
                                            continue;
                                        k32 = -1;
                                        break;
                                    }

                                    if (k32 != -1)
                                        ai[l16++] = k32;
                                }
                            }

                        }

                    }

                    terrainData = new byte[l16][];
                    aByteArrayArray1247 = new byte[l16][];
                    mapCoordinates = new int[l16];
                    terrainIndices = new int[l16];
                    anIntArray1236 = new int[l16];
                    for (int l26 = 0; l26 < l16; l26++) {
                        int i29 = mapCoordinates[l26] = ai[l26];
                        int l30 = i29 >> 8 & 0xff;
                        int l31 = i29 & 0xff;
                        int j32 = terrainIndices[l26] = onDemandFetcher.getMapIndex(0, l31, l30);
                        if (j32 != -1)
                            onDemandFetcher.method558(3, j32);
                        int i33 = anIntArray1236[l26] = onDemandFetcher.getMapIndex(1, l31, l30);
                        if (i33 != -1)
                            onDemandFetcher.method558(3, i33);
                    }

                }
                int i17 = baseX - anInt1036;
                int j21 = baseY - anInt1037;
                anInt1036 = baseX;
                anInt1037 = baseY;
                for (int j24 = 0; j24 < 16384; j24++) {
                    Npc npc = sessionNpcs[j24];
                    if (npc != null) {
                        for (int j29 = 0; j29 < 10; j29++) {
                            npc.pathX[j29] -= i17;
                            npc.pathY[j29] -= j21;
                        }

                        npc.boundExtentX -= i17 * 128;
                        npc.boundExtentY -= j21 * 128;
                    }
                }

                for (int i27 = 0; i27 < maxPlayers; i27++) {
                    Player player = session_players[i27];
                    if (player != null) {
                        for (int i31 = 0; i31 < 10; i31++) {
                            player.pathX[i31] -= i17;
                            player.pathY[i31] -= j21;
                        }

                        player.boundExtentX -= i17 * 128;
                        player.boundExtentY -= j21 * 128;
                    }
                }

                aBoolean1080 = true;
                byte byte1 = 0;
                byte byte2 = 104;
                byte byte3 = 1;
                if (i17 < 0) {
                    byte1 = 103;
                    byte2 = -1;
                    byte3 = -1;
                }
                byte byte4 = 0;
                byte byte5 = 104;
                byte byte6 = 1;
                if (j21 < 0) {
                    byte4 = 103;
                    byte5 = -1;
                    byte6 = -1;
                }
                for (int k33 = byte1; k33 != byte2; k33 += byte3) {
                    for (int l33 = byte4; l33 != byte5; l33 += byte6) {
                        int i34 = k33 + i17;
                        int j34 = l33 + j21;
                        for (int k34 = 0; k34 < 4; k34++)
                            if (i34 >= 0 && j34 >= 0 && i34 < 104 && j34 < 104)
                                groundArray[k34][k33][l33] = groundArray[k34][i34][j34];
                            else
                                groundArray[k34][k33][l33] = null;

                    }

                }

                for (GameObjectSpawnRequest gameObjectSpawnRequest_1 = (GameObjectSpawnRequest) aClass19_1179.getFront(); gameObjectSpawnRequest_1 != null; gameObjectSpawnRequest_1 = (GameObjectSpawnRequest) aClass19_1179.getNext()) {
                    gameObjectSpawnRequest_1.x -= i17;
                    gameObjectSpawnRequest_1.z -= j21;
                    if (gameObjectSpawnRequest_1.x < 0 || gameObjectSpawnRequest_1.z < 0 || gameObjectSpawnRequest_1.x >= 104 || gameObjectSpawnRequest_1.z >= 104)
                        gameObjectSpawnRequest_1.unlink();
                }

                if (destX != 0) {
                    destX -= i17;
                    destY -= j21;
                }
                aBoolean1160 = false;
                pktType = -1;
                return true;
            }
            if (pktType == 208) {
                int i3 = inStream.ig2b();
                if (i3 >= 0)
                    method60(i3);
                anInt1018 = i3;
                pktType = -1;
                return true;
            }
            if (pktType == 99)//minimap lock toggle
            {
                miniMapLock = inStream.g1();
                pktType = -1;
                return true;
            }
            if (pktType == 75) {
                int j3 = inStream.isg2();
                int j11 = inStream.isg2();
                RSInterface.interfaceCache[j11].anInt233 = 2;
                RSInterface.interfaceCache[j11].mediaID = j3;
                pktType = -1;
                return true;
            }
            if (pktType == 114) {
                anInt1104 = inStream.ig2() * 30;
                pktType = -1;
                return true;
            }
            if (pktType == 60) {
                anInt1269 = inStream.g1();
                anInt1268 = inStream.ng1b();
                while (inStream.pos < pktSize) {
                    int k3 = inStream.g1();
                    method137(inStream, k3);
                }
                pktType = -1;
                return true;
            }
            if (pktType == 35) {
                int l3 = inStream.g1();
                int k11 = inStream.g1();
                int j17 = inStream.g1();
                int k21 = inStream.g1();
                aBooleanArray876[l3] = true;
                anIntArray873[l3] = k11;
                anIntArray1203[l3] = j17;
                anIntArray928[l3] = k21;
                anIntArray1030[l3] = 0;
                pktType = -1;
                return true;
            }
            if (pktType == 174) {
                int i4 = inStream.g2();
                int l11 = inStream.g1();
                int k17 = inStream.g2();
                if (wave_on && !lowMem && anInt1062 < 50) {
                    anIntArray1207[anInt1062] = i4;
                    anIntArray1241[anInt1062] = l11;
                    anIntArray1250[anInt1062] = k17 + Sound.anIntArray326[i4];
                    anInt1062++;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 104) {
                int j4 = inStream.ng1b();
                int i12 = inStream.nsp1();
                String s6 = inStream.gstr();
                if (j4 >= 1 && j4 <= 5) {
                    if (s6.equalsIgnoreCase("null"))
                        s6 = null;
                    atPlayerActions[j4 - 1] = s6;
                    atPlayerArray[j4 - 1] = i12 == 0;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 78) {
                destX = 0;
                pktType = -1;
                return true;
            }
            if (pktType == 253) {
                String s = inStream.gstr();
                if (s.endsWith(":tradereq:")) {
                    String s3 = s.substring(0, s.indexOf(":"));
                    long l17 = TextClass.longForName(s3);
                    boolean flag2 = false;
                    for (int j27 = 0; j27 < user_ignore_count; j27++) {
                        if (user_ignore_names[j27] != l17)
                            continue;
                        flag2 = true;
                        break;
                    }

                    if (!flag2 && anInt1251 == 0)
                        pushMessage("wishes to trade with you.", 4, s3);
                } else if (s.endsWith(":duelreq:")) {
                    String s4 = s.substring(0, s.indexOf(":"));
                    long l18 = TextClass.longForName(s4);
                    boolean flag3 = false;
                    for (int k27 = 0; k27 < user_ignore_count; k27++) {
                        if (user_ignore_names[k27] != l18)
                            continue;
                        flag3 = true;
                        break;
                    }

                    if (!flag3 && anInt1251 == 0)
                        pushMessage("wishes to duel with you.", 8, s4);
                } else if (s.endsWith(":chalreq:")) {
                    String s5 = s.substring(0, s.indexOf(":"));
                    long l19 = TextClass.longForName(s5);
                    boolean flag4 = false;
                    for (int l27 = 0; l27 < user_ignore_count; l27++) {
                        if (user_ignore_names[l27] != l19)
                            continue;
                        flag4 = true;
                        break;
                    }

                    if (!flag4 && anInt1251 == 0) {
                        String s8 = s.substring(s.indexOf(":") + 1, s.length() - 9);
                        pushMessage(s8, 8, s5);
                    }
                } else {
                    pushMessage(s, 0, "");
                }
                pktType = -1;
                //serverMessage(s);

                return true;
            }
            if (pktType == 1) {
                for (int k4 = 0; k4 < session_players.length; k4++)
                    if (session_players[k4] != null)
                        session_players[k4].animation = -1;

                for (int j12 = 0; j12 < sessionNpcs.length; j12++)
                    if (sessionNpcs[j12] != null)
                        sessionNpcs[j12].animation = -1;

                pktType = -1;
                return true;
            }
            if (pktType == 50) {
                long l4 = inStream.g8();
                int i18 = inStream.g1();
                String s7 = TextClass.fixName(TextClass.nameForLong(l4));
                for (int k24 = 0; k24 < user_friends_count; k24++) {
                    if (l4 != friendsListAsLongs[k24])
                        continue;
                    if (user_friends_worldid[k24] != i18) {
                        user_friends_worldid[k24] = i18;
                        needDrawTabArea = true;
                        if (i18 > 0)
                            pushMessage(s7 + " has logged in.", 5, "");
                        if (i18 == 0)
                            pushMessage(s7 + " has logged out.", 5, "");
                    }
                    s7 = null;
                    break;
                }

                if (s7 != null && user_friends_count < 200) {
                    friendsListAsLongs[user_friends_count] = l4;
                    user_friends_name_string[user_friends_count] = s7;
                    user_friends_worldid[user_friends_count] = i18;
                    user_friends_count++;
                    needDrawTabArea = true;
                }
                for (boolean flag6 = false; !flag6; ) {
                    flag6 = true;
                    for (int k29 = 0; k29 < user_friends_count - 1; k29++)
                        if (user_friends_worldid[k29] != network_worldid && user_friends_worldid[k29 + 1] == network_worldid || user_friends_worldid[k29] == 0 && user_friends_worldid[k29 + 1] != 0) {
                            int j31 = user_friends_worldid[k29];
                            user_friends_worldid[k29] = user_friends_worldid[k29 + 1];
                            user_friends_worldid[k29 + 1] = j31;
                            String s10 = user_friends_name_string[k29];
                            user_friends_name_string[k29] = user_friends_name_string[k29 + 1];
                            user_friends_name_string[k29 + 1] = s10;
                            long l32 = friendsListAsLongs[k29];
                            friendsListAsLongs[k29] = friendsListAsLongs[k29 + 1];
                            friendsListAsLongs[k29 + 1] = l32;
                            needDrawTabArea = true;
                            flag6 = false;
                        }

                }

                pktType = -1;
                return true;
            }
            if (pktType == 110) {
                if (tabID == 12)
                    needDrawTabArea = true;
                user_energy = inStream.g1();
                pktType = -1;
                return true;
            }
            if (pktType == 254) {
                anInt855 = inStream.g1();
                if (anInt855 == 1)
                    anInt1222 = inStream.g2();
                if (anInt855 >= 2 && anInt855 <= 6) {
                    if (anInt855 == 2) {
                        anInt937 = 64;
                        anInt938 = 64;
                    }
                    if (anInt855 == 3) {
                        anInt937 = 0;
                        anInt938 = 64;
                    }
                    if (anInt855 == 4) {
                        anInt937 = 128;
                        anInt938 = 64;
                    }
                    if (anInt855 == 5) {
                        anInt937 = 64;
                        anInt938 = 0;
                    }
                    if (anInt855 == 6) {
                        anInt937 = 64;
                        anInt938 = 128;
                    }
                    anInt855 = 2;
                    anInt934 = inStream.g2();
                    anInt935 = inStream.g2();
                    anInt936 = inStream.g1();
                }
                if (anInt855 == 10)
                    anInt933 = inStream.g2();
                pktType = -1;
                return true;
            }
            if (pktType == 248) {
                int i5 = inStream.sg2();
                int k12 = inStream.g2();
                if (backDialogID != -1) {
                    backDialogID = -1;
                    inputTaken = true;
                }
                if (inputDialogState != 0) {
                    inputDialogState = 0;
                    inputTaken = true;
                }
                openInterfaceID = i5;
                invOverlayInterfaceID = k12;
                needDrawTabArea = true;
                tabAreaAltered = true;
                aBoolean1149 = false;
                pktType = -1;
                return true;
            }
            if (pktType == 79) {
                int j5 = inStream.ig2();
                int l12 = inStream.sg2();
                RSInterface class9_3 = RSInterface.interfaceCache[j5];
                if (class9_3 != null && class9_3.type == 0) {
                    if (l12 < 0)
                        l12 = 0;
                    if (l12 > class9_3.scrollMax - class9_3.height)
                        l12 = class9_3.scrollMax - class9_3.height;
                    class9_3.scrollPosition = l12;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 68) {
                for (int k5 = 0; k5 < sessionSettings.length; k5++)
                    if (sessionSettings[k5] != anIntArray1045[k5]) {
                        sessionSettings[k5] = anIntArray1045[k5];
                        applyConfigChange(k5);
                        needDrawTabArea = true;
                    }

                pktType = -1;
                return true;
            }
            if (pktType == 196) {
                long l5 = inStream.g8();
                int j18 = inStream.g4();
                int l21 = inStream.g1();
                boolean flag5 = false;
                for (int i28 = 0; i28 < 100; i28++) {
                    if (anIntArray1240[i28] != j18)
                        continue;
                    flag5 = true;
                    break;
                }

                if (l21 <= 1) {
                    for (int l29 = 0; l29 < user_ignore_count; l29++) {
                        if (user_ignore_names[l29] != l5)
                            continue;
                        flag5 = true;
                        break;
                    }

                }
                if (!flag5 && anInt1251 == 0)
                    try {
                        anIntArray1240[anInt1169] = j18;
                        anInt1169 = (anInt1169 + 1) % 100;
                        String s9 = TextInput.method525(pktSize - 13, inStream);
                        if (l21 != 3)
                            s9 = Censor.doCensor(s9);
                        if (l21 == 2 || l21 == 3)
                            pushMessage(s9, 7, "@cr2@" + TextClass.fixName(TextClass.nameForLong(l5)));
                        else if (l21 == 1)
                            pushMessage(s9, 7, "@cr1@" + TextClass.fixName(TextClass.nameForLong(l5)));
                        else
                            pushMessage(s9, 3, TextClass.fixName(TextClass.nameForLong(l5)));
                    } catch (Exception exception1) {
                        Signlink.reporterror("cde1");
                    }
                pktType = -1;
                return true;
            }
            if (pktType == 85) {
                anInt1269 = inStream.ng1b();
                anInt1268 = inStream.ng1b();
                pktType = -1;
                return true;
            }
            if (pktType == 24) {
                anInt1054 = inStream.sg1();
                if (anInt1054 == tabID) {
                    if (anInt1054 == 3)
                        tabID = 1;
                    else
                        tabID = 3;
                    needDrawTabArea = true;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 246) {
                int i6 = inStream.ig2();
                int i13 = inStream.g2();
                int k18 = inStream.g2();
                if (k18 == 65535) {
                    RSInterface.interfaceCache[i6].anInt233 = 0;
                    pktType = -1;
                    return true;
                } else {
                    ItemDef itemDef = ItemDef.forID(k18);
                    RSInterface.interfaceCache[i6].anInt233 = 4;
                    RSInterface.interfaceCache[i6].mediaID = k18;
                    RSInterface.interfaceCache[i6].anInt270 = itemDef.sprite_rotation_scale;
                    RSInterface.interfaceCache[i6].anInt271 = itemDef.modelRotation2;
                    RSInterface.interfaceCache[i6].anInt269 = (itemDef.modelZoom * 100) / i13;
                    pktType = -1;
                    return true;
                }
            }
            if (pktType == 171) {
                boolean flag1 = inStream.g1() == 1;
                int j13 = inStream.g2();
                RSInterface.interfaceCache[j13].hidden_until_mouseover = flag1;
                pktType = -1;
                return true;
            }
            if (pktType == 142) {
                int j6 = inStream.ig2();
                method60(j6);
                if (backDialogID != -1) {
                    backDialogID = -1;
                    inputTaken = true;
                }
                if (inputDialogState != 0) {
                    inputDialogState = 0;
                    inputTaken = true;
                }
                invOverlayInterfaceID = j6;
                needDrawTabArea = true;
                tabAreaAltered = true;
                openInterfaceID = -1;
                aBoolean1149 = false;
                pktType = -1;
                return true;
            }
            if (pktType == 126) {
                String s1 = inStream.gstr();
                int k13 = inStream.sg2();
                RSInterface.interfaceCache[k13].text_conditionfalse = s1;
                if (RSInterface.interfaceCache[k13].parentID == tabInterfaceIDs[tabID])
                    needDrawTabArea = true;
                pktType = -1;
                return true;
            }
            if (pktType == 206) {
                publicChatMode = inStream.g1();
                privateChatMode = inStream.g1();
                tradeMode = inStream.g1();
                aBoolean1233 = true;
                inputTaken = true;
                pktType = -1;
                return true;
            }
            if (pktType == 240) {
                if (tabID == 12)
                    needDrawTabArea = true;
                user_weight = inStream.g2b();
                pktType = -1;
                return true;
            }
            if (pktType == 8) {
                int k6 = inStream.isg2();
                int l13 = inStream.g2();
                RSInterface.interfaceCache[k6].anInt233 = 1;
                RSInterface.interfaceCache[k6].mediaID = l13;
                pktType = -1;
                return true;
            }
            if (pktType == 122) {
                int l6 = inStream.isg2();
                int i14 = inStream.isg2();
                int i19 = i14 >> 10 & 0x1f;
                int i22 = i14 >> 5 & 0x1f;
                int l24 = i14 & 0x1f;
                RSInterface.interfaceCache[l6].colour_conditionfalse = (i19 << 19) + (i22 << 11) + (l24 << 3);
                pktType = -1;
                return true;
            }
            if (pktType == 53) {
                needDrawTabArea = true;
                int i7 = inStream.g2();
                RSInterface class9_1 = RSInterface.interfaceCache[i7];
                int j19 = inStream.g2();
                for (int j22 = 0; j22 < j19; j22++) {
                    int i25 = inStream.g1();
                    if (i25 == 255)
                        i25 = inStream.biig4();
                    class9_1.inv[j22] = inStream.isg2();
                    class9_1.invStackSizes[j22] = i25;
                }

                for (int j25 = j19; j25 < class9_1.inv.length; j25++) {
                    class9_1.inv[j25] = 0;
                    class9_1.invStackSizes[j25] = 0;
                }

                pktType = -1;
                return true;
            }
            if (pktType == 230) {
                int j7 = inStream.sg2();
                int j14 = inStream.g2();
                int k19 = inStream.g2();
                int k22 = inStream.isg2();
                RSInterface.interfaceCache[j14].anInt270 = k19;
                RSInterface.interfaceCache[j14].anInt271 = k22;
                RSInterface.interfaceCache[j14].anInt269 = j7;
                pktType = -1;
                return true;
            }
            if (pktType == 221) {
                network_friends_server_status = inStream.g1();
                needDrawTabArea = true;
                pktType = -1;
                return true;
            }
            if (pktType == 177) {
                aBoolean1160 = true;
                anInt995 = inStream.g1();
                anInt996 = inStream.g1();
                anInt997 = inStream.g2();
                anInt998 = inStream.g1();
                anInt999 = inStream.g1();
                if (anInt999 >= 100) {
                    int k7 = anInt995 * 128 + 64;
                    int k14 = anInt996 * 128 + 64;
                    int i20 = method42(plane, k14, k7) - anInt997;
                    int l22 = k7 - xCameraPos;
                    int k25 = i20 - zCameraPos;
                    int j28 = k14 - yCameraPos;
                    int i30 = (int) Math.sqrt(l22 * l22 + j28 * j28);
                    yCameraCurve = (int) (Math.atan2(k25, i30) * 325.94900000000001D) & 0x7ff;
                    xCameraCurve = (int) (Math.atan2(l22, j28) * -325.94900000000001D) & 0x7ff;
                    if (yCameraCurve < 128)
                        yCameraCurve = 128;
                    if (yCameraCurve > 383)
                        yCameraCurve = 383;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 249) {
                anInt1046 = inStream.nsp1();
                unknownInt10 = inStream.isg2();
                pktType = -1;
                return true;
            }
            if (pktType == 65) {
                getNpcPos(inStream, pktSize);
                pktType = -1;
                return true;
            }
            if (pktType == 27) {
                messagePromptRaised = false;
                inputDialogState = 1;
                amountOrNameInput = "";
                inputTaken = true;
                pktType = -1;
                return true;
            }
            if (pktType == 187) {
                messagePromptRaised = false;
                inputDialogState = 2;
                amountOrNameInput = "";
                inputTaken = true;
                pktType = -1;
                return true;
            }
            if (pktType == 97) {
                int l7 = inStream.g2();
                method60(l7);
                if (invOverlayInterfaceID != -1) {
                    invOverlayInterfaceID = -1;
                    needDrawTabArea = true;
                    tabAreaAltered = true;
                }
                if (backDialogID != -1) {
                    backDialogID = -1;
                    inputTaken = true;
                }
                if (inputDialogState != 0) {
                    inputDialogState = 0;
                    inputTaken = true;
                }
                openInterfaceID = l7;
                aBoolean1149 = false;
                pktType = -1;
                return true;
            }
            if (pktType == 218) {
                int i8 = inStream.isg2b();
                dialogID = i8;
                inputTaken = true;
                pktType = -1;
                return true;
            }
            if (pktType == 87) {
                int j8 = inStream.ig2();
                int l14 = inStream.big4();
                anIntArray1045[j8] = l14;
                if (sessionSettings[j8] != l14) {
                    sessionSettings[j8] = l14;
                    applyConfigChange(j8);
                    needDrawTabArea = true;
                    if (dialogID != -1)
                        inputTaken = true;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 36) {
                int k8 = inStream.ig2();
                byte byte0 = inStream.g1b();
                anIntArray1045[k8] = byte0;
                if (sessionSettings[k8] != byte0) {
                    sessionSettings[k8] = byte0;
                    applyConfigChange(k8);
                    needDrawTabArea = true;
                    if (dialogID != -1)
                        inputTaken = true;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 61) {
                anInt1055 = inStream.g1();
                pktType = -1;
                return true;
            }
            if (pktType == 200) {
                int l8 = inStream.g2();
                int i15 = inStream.g2b();
                RSInterface class9_4 = RSInterface.interfaceCache[l8];
                class9_4.animation_conditionfalse = i15;
                if (i15 == -1) {
                    class9_4.anim_frame = 0;
                    class9_4.anInt208 = 0;
                }
                pktType = -1;
                return true;
            }
            if (pktType == 219) {
                if (invOverlayInterfaceID != -1) {
                    invOverlayInterfaceID = -1;
                    needDrawTabArea = true;
                    tabAreaAltered = true;
                }
                if (backDialogID != -1) {
                    backDialogID = -1;
                    inputTaken = true;
                }
                if (inputDialogState != 0) {
                    inputDialogState = 0;
                    inputTaken = true;
                }
                openInterfaceID = -1;
                aBoolean1149 = false;
                pktType = -1;
                return true;
            }
            if (pktType == 34) {
                needDrawTabArea = true;
                int i9 = inStream.g2();
                RSInterface class9_2 = RSInterface.interfaceCache[i9];
                while (inStream.pos < pktSize) {
                    int j20 = inStream.gsmarts();
                    int i23 = inStream.g2();
                    int l25 = inStream.g1();
                    if (l25 == 255)
                        l25 = inStream.g4();
                    if (j20 >= 0 && j20 < class9_2.inv.length) {
                        class9_2.inv[j20] = i23;
                        class9_2.invStackSizes[j20] = l25;
                    }
                }
                pktType = -1;
                return true;
            }
            if (pktType == 105 || pktType == 84 || pktType == 147 || pktType == 215 || pktType == 4 || pktType == 117 || pktType == 156 || pktType == 44 || pktType == 160 || pktType == 101 || pktType == 151) {
                method137(inStream, pktType);
                pktType = -1;
                return true;
            }
            if (pktType == 106) {
                tabID = inStream.ng1b();
                needDrawTabArea = true;
                tabAreaAltered = true;
                pktType = -1;
                return true;
            }
            if (pktType == 164) {
                int j9 = inStream.ig2();
                method60(j9);
                if (invOverlayInterfaceID != -1) {
                    invOverlayInterfaceID = -1;
                    needDrawTabArea = true;
                    tabAreaAltered = true;
                }
                backDialogID = j9;
                inputTaken = true;
                openInterfaceID = -1;
                aBoolean1149 = false;
                pktType = -1;
                return true;
            }
            Signlink.reporterror("T1 - " + pktType + "," + pktSize + " - " + anInt842 + "," + anInt843);
            network_disconnect();
        } catch (IOException _ex) {
            dropClient();
        } catch (Exception exception) {
            String s2 = "T2 - " + pktType + "," + anInt842 + "," + anInt843 + " - " + pktSize + "," + (baseX + session_player.pathX[0]) + "," + (baseY + session_player.pathY[0]) + " - ";
            for (int j15 = 0; j15 < pktSize && j15 < 50; j15++)
                s2 = s2 + inStream.data[j15] + ",";

            Signlink.reporterror(s2);
            network_disconnect();
            exception.printStackTrace();
        }
        return true;
    }

    public void renderscene() {
        sceneGraph.render(xCameraPos, yCameraPos, xCameraCurve, zCameraPos, fieldJ, yCameraCurve);
    }

    private void method146() {
        anInt1265++;
        world_post_players_to_renderer(true);
        renderNPCs(true);
        world_post_players_to_renderer(false);
        renderNPCs(false);
        renderProjectiles();
        renderStationaryGraphics();
        if (!aBoolean1160) {
            int i = anInt1184;
            if (anInt984 / 256 > i)
                i = anInt984 / 256;
            if (aBooleanArray876[4] && anIntArray1203[4] + 128 > i)
                i = anIntArray1203[4] + 128;
            int k = minimapInt1 + anInt896 & 0x7ff;
            setCameraPos(600 + i * 3, i, anInt1014, method42(plane, session_player.boundExtentY, session_player.boundExtentX) - 50, k, anInt1015);
        }
        int j;
        if (!aBoolean1160)
            j = method120();
        else
            j = resetCameraHeight();
        int l = xCameraPos;
        int i1 = zCameraPos;
        int j1 = yCameraPos;
        int k1 = yCameraCurve;
        int l1 = xCameraCurve;
        for (int i2 = 0; i2 < 5; i2++)
            if (aBooleanArray876[i2]) {
                int j2 = (int) ((Math.random() * (double) (anIntArray873[i2] * 2 + 1) - (double) anIntArray873[i2]) + Math.sin((double) anIntArray1030[i2] * ((double) anIntArray928[i2] / 100D)) * (double) anIntArray1203[i2]);
                if (i2 == 0)
                    xCameraPos += j2;
                if (i2 == 1)
                    zCameraPos += j2;
                if (i2 == 2)
                    yCameraPos += j2;
                if (i2 == 3)
                    xCameraCurve = xCameraCurve + j2 & 0x7ff;
                if (i2 == 4) {
                    yCameraCurve += j2;
                    if (yCameraCurve < 128)
                        yCameraCurve = 128;
                    if (yCameraCurve > 383)
                        yCameraCurve = 383;
                }
            }

        int k2 = Rasterizer.textureGetCount;
        Model.aBoolean1684 = true;
        Model.resourceCount = 0;
        Model.cursorXPos = super.mouseEventX - 4;
        Model.cursorYPos = super.mouseEventY - 4;
        Graphics2D.resetImage();
        //xxx disables graphics            if(graphicsEnabled){
        if (pglWrapper != null) {
            pglWrapper.setCameraPosition(xCameraPos, yCameraPos, zCameraPos);
            pglWrapper.setCameraRotation(-xCameraCurve, 0, yCameraCurve);
        }
        fieldJ = j;
        //sceneGraph.render(xCameraPos, yCameraPos, xCameraCurve, zCameraPos, j, yCameraCurve);
        if (pglWrapper != null && renderNode == null) {
            renderNode = new PglCallClientNode();
            pglWrapper.scene.add(renderNode);
        }  else
            renderscene();
        if (pglWrapper != null)
            pglWrapper.process();
        sceneGraph.clearInteractableObjectCache();
        updateEntities();
        drawHeadIcon();
        method37(k2);
        draw3dScreen();
        gameScreenCanvas.drawGraphics(4, super.graphics, 4);
        xCameraPos = l;
        zCameraPos = i1;
        yCameraPos = j1;
        yCameraCurve = k1;
        xCameraCurve = l1;
        //            }
    }

    private void clearTopInterfaces() {
        stream.p1isaac(130);
        if (invOverlayInterfaceID != -1) {
            invOverlayInterfaceID = -1;
            needDrawTabArea = true;
            aBoolean1149 = false;
            tabAreaAltered = true;
        }
        if (backDialogID != -1) {
            backDialogID = -1;
            inputTaken = true;
            aBoolean1149 = false;
        }
        openInterfaceID = -1;
    }

    public Client() {
        anIntArrayArray825 = new int[104][104];
        user_friends_worldid = new int[200];
        groundArray = new Deque[4][104][104];
        aBoolean831 = false;
        aStream_834 = new Packet(new byte[5000]);
        sessionNpcs = new Npc[16384];
        sessionNpcList = new int[16384];
        anIntArray840 = new int[1000];
        aStream_847 = Packet.create();
        wave_on = true;
        openInterfaceID = -1;
        user_experience = new int[Skills.COUNT];
        aBoolean872 = false;
        anIntArray873 = new int[5];
        anInt874 = -1;
        aBooleanArray876 = new boolean[5];
        drawFlames = false;
        reportAbuseInput = "";
        unknownInt10 = -1;
        menuOpen = false;
        inputString = "";
        maxPlayers = 2048;
        session_player_idx = 2047;
        session_players = new Player[maxPlayers];
        session_player_list = new int[maxPlayers];
        session_npcs_awaiting_update = new int[maxPlayers];
        aStreamArray895s = new Packet[maxPlayers];
        anInt897 = 1;
        anIntArrayArray901 = new int[104][104];
        anInt902 = 0x766654;
        aByteArray912 = new byte[16384];
        user_levels = new int[Skills.COUNT];
        user_ignore_names = new long[100];
        loadingError = false;
        anInt927 = 0x332d25;
        anIntArray928 = new int[5];
        anIntArrayArray929 = new int[104][104];
        chatTypes = new int[100];
        chatNames = new String[100];
        chatMessages = new String[100];
        sideIcons = new IndexedImage[13];
        aBoolean954 = true;
        friendsListAsLongs = new long[200];
        currentSong = -1;
        drawingFlames = false;
        spriteDrawX = -1;
        spriteDrawY = -1;
        compassShape1 = new int[33];
        anIntArray969 = new int[256];
        jagexFileStores = new JagexFileStore[5];
        sessionSettings = new int[2000];
        aBoolean972 = false;
        anInt975 = 50;
        anIntArray976 = new int[anInt975];
        anIntArray977 = new int[anInt975];
        anIntArray978 = new int[anInt975];
        anIntArray979 = new int[anInt975];
        anIntArray980 = new int[anInt975];
        anIntArray981 = new int[anInt975];
        anIntArray982 = new int[anInt975];
        aStringArray983 = new String[anInt975];
        anInt985 = -1;
        hitMarks = new RgbImage[20];
        char_edit_colours = new int[5];
        aBoolean994 = false;
        anInt1002 = 0x23201b;
        amountOrNameInput = "";
        aClass19_1013 = new Deque();
        aBoolean1017 = false;
        anInt1018 = -1;
        anIntArray1030 = new int[5];
        char_edit_model_changed = false;
        mapFunctions = new RgbImage[100];
        dialogID = -1;
        user_stats = new int[Skills.COUNT];
        anIntArray1045 = new int[2000];
        char_edit_gender = true;
        minimapShape1 = new int[151];
        anInt1054 = -1;
        aClass19_1056 = new Deque();
        compassShape2 = new int[33];
        aClass9_1059 = new RSInterface();
        mapScenes = new IndexedImage[100];
        anInt1063 = 0x4d4233;
        char_edit_idkits = new int[7];
        markPosX = new int[1000];
        markPosY = new int[1000];
        aBoolean1080 = false;
        user_friends_name_string = new String[200];
        inStream = Packet.create();
        expectedCRCs = new int[9];
        menuActionCmd2 = new int[500];
        menuActionCmd3 = new int[500];
        menuActionID = new int[500];
        menuActionCmd1 = new int[500];
        headIcons = new RgbImage[20];
        tabAreaAltered = false;
        aString1121 = "";
        atPlayerActions = new String[5];
        atPlayerArray = new boolean[5];
        anIntArrayArrayArray1129 = new int[4][13][13];
        anInt1132 = 2;
        markGraphic = new RgbImage[1000];
        aBoolean1141 = false;
        aBoolean1149 = false;
        crosses = new RgbImage[8];
        musicEnabled = true;
        needDrawTabArea = false;
        loggedIn = false;
        report_abuse_mute_player = false;
        aBoolean1159 = false;
        aBoolean1160 = false;
        anInt1171 = 1;
        myUsername = "";
        myPassword = "";
        genericLoadingError = false;
        reportAbuseInterfaceID = -1;
        aClass19_1179 = new Deque();
        anInt1184 = 128;
        invOverlayInterfaceID = -1;
        stream = Packet.create();
        menuActionName = new String[500];
        anIntArray1203 = new int[5];
        anIntArray1207 = new int[50];
        anInt1210 = 2;
        anInt1211 = 78;
        promptInput = "";
        modIcons = new IndexedImage[2];
        tabID = 3;
        inputTaken = false;
        songChanging = true;
        minimapShape2 = new int[151];
        tileSettings = new TileSetting[4];
        aBoolean1233 = false;
        anIntArray1240 = new int[100];
        anIntArray1241 = new int[50];
        aBoolean1242 = false;
        anIntArray1250 = new int[50];
        rsAlreadyLoaded = false;
        repaintRequested = false;
        messagePromptRaised = false;
        loginMessage1 = "";
        loginMessage2 = "";
        backDialogID = -1;
        anInt1279 = 2;
        bigX = new int[4000];
        bigY = new int[4000];
        anInt1289 = -1;

    }

    //start of 484 orbs
    private RgbImage[] statusGlobes = new RgbImage[11];
    private boolean runIsOn = false;
    private int statusGlobesHover = -1;

    public int calcGlobeTextColor(int percent) {
        if (percent > 75) {
            return 0x00ff00;
        } else if (percent > 50) {
            return 0xffff00;
        } else if (percent > 25) {
            return 0xff981f;
        } else {
            return 0xff0000;
        }
    }

    @SuppressWarnings("unused")
    private void drawStatusGlobes() {
        // Hit-points
        System.out.println("draw orb");
        int hpPercent = 100 * user_levels[3] / user_stats[3];
        statusGlobes[0].drawSprite(171, 11);
        statusGlobes[1].drawSprite(174, 14);
        statusGlobes[0].drawSpriteWithDimens(171, 11, 34 - (user_levels[3] * 34 / user_stats[3]));
        if (hpPercent > 25) {
            statusGlobes[5].drawSprite(180, 22);
        } else {
            if (currentTime % 20 <= 10)
                statusGlobes[5].drawSprite(180, 22);
            if (currentTime % 20 <= 5)
                statusGlobes[6].drawSprite(180, 22);
        }
        smallFont.method382(calcGlobeTextColor(hpPercent), 215, "" + user_levels[3], 39, true);
        // Prayer
        int prayerPercent = 100 * user_levels[5] / user_stats[5];
        statusGlobes[0].drawSprite(187, 50);
        statusGlobes[2].drawSprite(190, 53);
        statusGlobes[0].drawSpriteWithDimens(187, 50, 34 - (user_levels[5] * 34 / user_stats[5]));
        statusGlobes[7].drawSprite(193, 57);
        smallFont.method382(calcGlobeTextColor(prayerPercent), 232, "" + user_levels[5], 78, true);
        // Run
        int runPercent = user_energy;
        if (statusGlobesHover == -1) {
            if (runIsOn == false) {
                statusGlobes[0].drawSprite(187, 89);
                statusGlobes[3].drawSprite(190, 92);
                statusGlobes[0].drawSpriteWithDimens(187, 89, 34 - (runPercent * 34 / 100));
                statusGlobes[8].drawSprite(197, 95);
            } else if (runIsOn == true) {
                statusGlobes[0].drawSprite(187, 89);
                statusGlobes[4].drawSprite(190, 92);
                statusGlobes[0].drawSpriteWithDimens(187, 89, 34 - (runPercent * 34 / 100));
                statusGlobes[9].drawSprite(197, 96);
            }
        } else if (statusGlobesHover == 0) {
            if (runIsOn == false) {
                statusGlobes[10].drawSprite(187, 89);
                statusGlobes[3].drawSprite(190, 92);
                statusGlobes[10].drawSpriteWithDimens(187, 89, 34 - (runPercent * 34 / 100));
                statusGlobes[8].drawSprite(197, 95);
            } else if (runIsOn == true) {
                statusGlobes[10].drawSprite(187, 89);
                statusGlobes[4].drawSprite(190, 92);
                statusGlobes[10].drawSpriteWithDimens(187, 89, 34 - (runPercent * 34 / 100));
                statusGlobes[9].drawSprite(197, 96);
            }
        }
        smallFont.method382(calcGlobeTextColor(runPercent), 232, "" + user_energy, 117, true);
    }
    //end of 484 orbs

    private int user_ignore_count;
    private long aLong824;
    private int[][] anIntArrayArray825;
    private int[] user_friends_worldid;
    private Deque[][][] groundArray;
    private int[] anIntArray828;
    private int[] anIntArray829;
    private volatile boolean aBoolean831;
    private Socket aSocket832;
    private int loginScreenState;
    private Packet aStream_834;
    private Npc[] sessionNpcs;
    private int sessionNpcCount;
    private int[] sessionNpcList;
    private int anInt839;
    private int[] anIntArray840;
    private int anInt841;
    private int anInt842;
    private int anInt843;
    private String aString844;
    private int privateChatMode;
    private Packet aStream_847;
    private boolean wave_on;
    private static int anInt849;
    private int[] anIntArray850;
    private int[] anIntArray851;
    private int[] anIntArray852;
    private int[] anIntArray853;
    private static int anInt854;
    private int anInt855;
    private int openInterfaceID;
    private int xCameraPos;
    private int zCameraPos;
    private int yCameraPos;
    private int yCameraCurve;
    private int xCameraCurve;
    private int myPrivilege;
    private final int[] user_experience;
    private IndexedImage redStone1_3;
    private IndexedImage redStone2_3;
    private IndexedImage redStone3_2;
    private IndexedImage redStone1_4;
    private IndexedImage redStone2_4;
    private RgbImage mapFlag;
    private RgbImage mapMarker;
    private boolean aBoolean872;
    private final int[] anIntArray873;
    private int anInt874;
    private final boolean[] aBooleanArray876;
    private int user_weight;
    private MouseDetection mouseDetection;
    private volatile boolean drawFlames;
    private String reportAbuseInput;
    private int unknownInt10;
    private boolean menuOpen;
    private int anInt886;
    private String inputString;
    private final int maxPlayers;
    private final int session_player_idx;
    private Player[] session_players;
    private int session_player_count;
    private int[] session_player_list;
    private int sessionNpcsAwaitingUpdatePtr;
    private int[] session_npcs_awaiting_update;
    private Packet[] aStreamArray895s;
    private int anInt896;
    private int anInt897;
    private int user_friends_count;
    private int network_friends_server_status;
    private int[][] anIntArrayArray901;
    private final int anInt902;
    private GraphicsBuffer backLeftIP1;
    private GraphicsBuffer backLeftIP2;
    private GraphicsBuffer backRightIP1;
    private GraphicsBuffer backRightIP2;
    private GraphicsBuffer backTopIP1;
    private GraphicsBuffer backVmidIP1;
    private GraphicsBuffer backVmidIP2;
    private GraphicsBuffer backVmidIP3;
    private GraphicsBuffer backVmidIP2_2;
    private byte[] aByteArray912;
    private int anInt913;
    private int crossX;
    private int crossY;
    private int crossIndex;
    private int crossType;
    private int plane;
    private final int[] user_levels;
    private static int anInt924;
    private final long[] user_ignore_names;
    private boolean loadingError;
    private final int anInt927;
    private final int[] anIntArray928;
    private int[][] anIntArrayArray929;
    private RgbImage char_edit_inactive_button;
    private RgbImage char_edit_active_button;
    private int anInt933;
    private int anInt934;
    private int anInt935;
    private int anInt936;
    private int anInt937;
    private int anInt938;
    private static int anInt940;
    private final int[] chatTypes;
    private final String[] chatNames;
    private final String[] chatMessages;
    private int anInt945;
    private SceneGraph sceneGraph;
    private IndexedImage[] sideIcons;
    private int menuScreenArea;
    private int menuOffsetX;
    private int menuOffsetY;
    private int menuWidth;
    private int anInt952;
    private long aLong953;
    private boolean aBoolean954;
    private long[] friendsListAsLongs;
    private int currentSong;
    private static int network_worldid = 10;
    static int portOff;
    private static boolean isMembers = true;
    private static boolean lowMem;
    private volatile boolean drawingFlames;
    private int spriteDrawX;
    private int spriteDrawY;
    private final int[] anIntArray965 = {0xffff00, 0xff0000, 65280, 65535, 0xff00ff, 0xffffff};
    //private rs2.IndexedImage aIndexedImage_966;//never used
    //private rs2.IndexedImage aIndexedImage_999;//never used
    //private rs2.IndexedImage aIndexedImage_967;//never used

    private final int[] compassShape1;
    private final int[] anIntArray969;
    final JagexFileStore[] jagexFileStores;
    public int sessionSettings[];
    private boolean aBoolean972;
    private final int anInt975;
    private final int[] anIntArray976;
    private final int[] anIntArray977;
    private final int[] anIntArray978;
    private final int[] anIntArray979;
    private final int[] anIntArray980;
    private final int[] anIntArray981;
    private final int[] anIntArray982;
    private final String[] aStringArray983;
    private int anInt984;
    private int anInt985;
    private static int anInt986;
    private RgbImage[] hitMarks;
    private RgbImage titlebox;
    private RgbImage passwordhover;
    private RgbImage loginhover;
    private RgbImage loadingBox1;

    private int anInt988;
    private int anInt989;
    private final int[] char_edit_colours;
    private static boolean aBoolean993;
    private final boolean aBoolean994;
    private int anInt995;
    private int anInt996;
    private int anInt997;
    private int anInt998;
    private int anInt999;
    private IsaacCipher encryption;
    private RgbImage mapEdge;
    private final int anInt1002;
    static final int[][] playerBodyRecolours = {{6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 2983, 54193}, {8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003, 25239}, {25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003}, {4626, 11146, 6439, 12, 4758, 10270}, {4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574}};
    private String amountOrNameInput;
    private static int anInt1005;
    private int welcome_screen_last_login;
    private int pktSize;
    private int pktType;
    private int anInt1009;
    private int anInt1010;
    private int anInt1011;
    private Deque aClass19_1013;
    private int anInt1014;
    private int anInt1015;
    private int anInt1016;
    private boolean aBoolean1017;
    private int anInt1018;
    private static final int[] XP_FOR_LEVEL;
    private int miniMapLock;
    private int anInt1022;
    private int loadingStage;
    private IndexedImage scrollBar1;
    private IndexedImage scrollBar2;
    private int anInt1026;
    private IndexedImage backBase1;
    private IndexedImage backBase2;
    private IndexedImage backHmid1;
    private final int[] anIntArray1030;
    private boolean char_edit_model_changed;
    private RgbImage[] mapFunctions;
    private int baseX;
    private int baseY;
    private int anInt1036;
    private int anInt1037;
    private int loginFailures;
    private int anInt1039;
    private int anInt1040;
    private int anInt1041;
    private int dialogID;
    private final int[] user_stats;
    private final int[] anIntArray1045;
    private int anInt1046;
    private boolean char_edit_gender;
    private int anInt1048;
    private String aString1049;
    private static int anInt1051;
    private final int[] minimapShape1;
    private JagexArchive titleJagexArchive;
    private int anInt1054;
    private int anInt1055;
    private Deque aClass19_1056;
    private final int[] compassShape2;
    private final RSInterface aClass9_1059;
    private IndexedImage[] mapScenes;
    private static int anInt1061;
    private int anInt1062;
    private final int anInt1063;
    private int friendsListAction;
    private final int[] char_edit_idkits;
    private int mouseInvInterfaceIndex;
    private int lastActiveInvInterface;
    private OnDemandFetcher onDemandFetcher;
    private int anInt1069;
    private int anInt1070;
    private int numOfMapMarkers;
    private int[] markPosX;
    private int[] markPosY;
    private RgbImage mapDotItem;
    private RgbImage mapDotNPC;
    private RgbImage mapDotPlayer;
    private RgbImage mapDotFriend;
    private RgbImage mapDotTeam;
    private int anInt1079;
    private boolean aBoolean1080;
    private String[] user_friends_name_string;
    private Packet inStream;
    private int anInt1084;
    private int anInt1085;
    private int activeInterfaceType;
    private int anInt1087;
    private int anInt1088;
    private int anInt1089;
    private final int[] expectedCRCs;
    private int[] menuActionCmd2;
    private int[] menuActionCmd3;
    private int[] menuActionID;
    private int[] menuActionCmd1;
    private RgbImage[] headIcons;
    private static int anInt1097;
    private int anInt1098;
    private int anInt1099;
    private int anInt1100;
    private int anInt1101;
    private int anInt1102;
    private boolean tabAreaAltered;
    private int anInt1104;
    private GraphicsBuffer aRSImageProducer_1107;
    private GraphicsBuffer aRSImageProducer_1108;
    private GraphicsBuffer aRSImageProducer_1109;
    private GraphicsBuffer aRSImageProducer_1110;
    private GraphicsBuffer aRSImageProducer_1111;
    private GraphicsBuffer aRSImageProducer_1112;
    private GraphicsBuffer aRSImageProducer_1113;
    private GraphicsBuffer aRSImageProducer_1114;
    private GraphicsBuffer aRSImageProducer_1115;
    private static int anInt1117;
    private int membersInt;
    private String aString1121;
    private RgbImage compass;
    private GraphicsBuffer aRSImageProducer_1123;
    private GraphicsBuffer aRSImageProducer_1124;
    private GraphicsBuffer aRSImageProducer_1125;
    public static Player session_player;
    private final String[] atPlayerActions;
    private final boolean[] atPlayerArray;
    private final int[][][] anIntArrayArrayArray1129;
    private final int[] tabInterfaceIDs = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private int anInt1131;
    private int anInt1132;
    private int menuActionRow;
    private static int anInt1134;
    private int spellSelected;
    private int anInt1137;
    private int spellUsableOn;
    private String spellTooltip;
    private RgbImage[] markGraphic;
    private boolean aBoolean1141;
    private static int anInt1142;
    private IndexedImage redStone1;
    private IndexedImage redStone2;
    private IndexedImage redStone3;
    private IndexedImage redStone1_2;
    private IndexedImage redStone2_2;
    private int user_energy;
    private boolean aBoolean1149;
    private RgbImage[] crosses;
    private boolean musicEnabled;
    private IndexedImage[] aIndexedImageArray1152s;
    private boolean needDrawTabArea;
    private int welcome_screen_unread_pm_count;
    private static int anInt1155;
    private static boolean fpsOn;
    public boolean loggedIn;
    private boolean report_abuse_mute_player;
    private boolean aBoolean1159;
    private boolean aBoolean1160;
    static int currentTime;
    private static final String validUserPassChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
    private GraphicsBuffer tabAreaDrawingTarget;
    private GraphicsBuffer minimapIP;
    private GraphicsBuffer gameScreenCanvas;
    private GraphicsBuffer aRSImageProducer_1166;
    private int welcome_screen_last_recovery_change_or_member_warning;
    private RSSocket socketStream;
    private int anInt1169;
    private int minimapInt3;
    private int anInt1171;
    private long aLong1172;
    private String myUsername;
    private String myPassword;
    private static int anInt1175;
    private boolean genericLoadingError;
    private final int[] anIntArray1177 = {0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3};
    private int reportAbuseInterfaceID;
    private Deque aClass19_1179;
    private int[] anIntArray1180;
    private int[] anIntArray1181;
    private int[] anIntArray1182;
    private byte[][] terrainData;
    private int anInt1184;
    private int minimapInt1;
    private int anInt1186;
    private int anInt1187;
    private static int anInt1188;
    private int invOverlayInterfaceID;
    private int[] anIntArray1190;
    private int[] anIntArray1191;
    private Packet stream;
    private int anInt1193;
    private int ui_split_private_chat;
    private IndexedImage invBack;
    private IndexedImage mapBack;
    private IndexedImage chatBack;
    private String[] menuActionName;
    private RgbImage aClass30_Sub2_Sub1_Sub1_1201;
    private RgbImage aClass30_Sub2_Sub1_Sub1_1202;
    private final int[] anIntArray1203;
    static final int[] skinColours = {9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 58654, 5027, 1457, 16565, 34991, 25486};
    private static boolean flagged;
    private final int[] anIntArray1207;
    private int anInt1208;
    private int minimapInt2;
    private int anInt1210;
    private int anInt1211;
    private String promptInput;
    private int anInt1213;
    private int[][][] intGroundArray;
    private long aLong1215;
    private int loginScreenCursorPos;
    private final IndexedImage[] modIcons;
    private long aLong1220;
    private int tabID;
    private int anInt1222;
    private boolean inputTaken;
    private int inputDialogState;
    private static int anInt1226;
    private int nextSong;
    private boolean songChanging;
    private final int[] minimapShape2;
    private TileSetting[] tileSettings;
    private boolean aBoolean1233;
    private int[] mapCoordinates;
    private int[] terrainIndices;
    private int[] anIntArray1236;
    private int anInt1237;
    private int anInt1238;
    public final int anInt1239 = 100;
    private final int[] anIntArray1240;
    private final int[] anIntArray1241;
    private boolean aBoolean1242;
    private int atInventoryLoopCycle;
    private int atInventoryInterface;
    private int atInventoryIndex;
    private int atInventoryInterfaceType;
    private byte[][] aByteArrayArray1247;
    private int tradeMode;
    private int anInt1249;
    private final int[] anIntArray1250;
    private int anInt1251;
    private final boolean rsAlreadyLoaded;
    private int anInt1253;
    private int anInt1254;
    private boolean repaintRequested;
    private boolean messagePromptRaised;
    private int anInt1257;
    private byte[][][] tileSettingBits;
    private int prevSong;
    private int destX;
    private int destY;
    private RgbImage minimapImage;
    private int anInt1264;
    private int anInt1265;
    private String loginMessage1;
    private String loginMessage2;
    private int anInt1268;
    private int anInt1269;
    private RSFont smallFont;
    private RSFont plainFont;
    private RSFont boldFont;
    private int anInt1275;
    private int backDialogID;
    private int anInt1278;
    private int anInt1279;
    private int[] bigX;
    private int[] bigY;
    private int itemSelected;
    private int anInt1283;
    private int anInt1284;
    private int anInt1285;
    private String selectedItemName;
    private int publicChatMode;
    private static int anInt1288;
    private int anInt1289;
    public static int anInt1290;
    public String server = "";
    public static boolean guiLaunch = true;

    static {
        XP_FOR_LEVEL = new int[99];
        int i = 0;
        for (int j = 0; j < 99; j++) {
            int l = j + 1;
            int i1 = (int) ((double) l + 300D * Math.pow(2D, (double) l / 7D));
            i += i1;
            XP_FOR_LEVEL[j] = i / 4;
        }

    }
}