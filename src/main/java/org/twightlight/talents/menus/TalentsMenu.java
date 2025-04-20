package org.twightlight.talents.menus;

import org.twightlight.talents.menus.buttons.intersection.*;
import org.twightlight.talents.menus.buttons.melee.*;
import org.twightlight.talents.menus.buttons.miscellaneous.*;
import org.twightlight.talents.menus.buttons.protective.*;
import org.twightlight.talents.menus.buttons.ranged.*;
import org.twightlight.talents.menus.buttons.ranged.SLS;
import org.twightlight.talents.menus.buttons.special.KOBW;
import org.twightlight.talents.menus.buttons.supportive.*;

import java.util.HashMap;

public class TalentsMenu {
    public static HashMap<Integer, HashMap<Integer, Button>> menuItems = new HashMap<>();

    public static void setItem(int x, int y, Button button) {
        menuItems.computeIfAbsent(x, k -> new HashMap<>());
        menuItems.get(x).put(y, button);
    }

    public static Button getButton(int x, int y) {
        if (menuItems.get(x) == null) {
            return Collections.getEmptyButton();
        }
        return (menuItems.get(x).get(y) != null) ? menuItems.get(x).get(y) : Collections.getEmptyButton();
    }

    public static void load() {
        //intersection
        new IMD();
        new MLS();
        new IHE();
        new AP();
        new CD();
        //melee
        new FA();
        new MAD();
        new MSL();
        new org.twightlight.talents.menus.buttons.melee.SLS();
        new MAS();
        new AMA();
        new AMAD();
        new CH();
        new TH();
        //ranged
        new AAD();
        new FRZ();
        new SA();
        new SLS();
        new AKB();
        new SM();
        new DLC();
        new CA();
        new TWIN();
        //protective
        new MHP();
        new YH();
        new FDR();
        new DR();
        new SRG();
        new ABS();
        new GNT();
        new CDR();
        new BL();
        new BLC();
        new THR();
        new RFL();
        //miscellaneous
        new IW();
        new IWD();
        new IES();
        new FBD();
        new TNTD();
        new EMS();
        new GGF();
        new WT();
        new WDT();
        new MSS();
        new EST();
        new IRS();
        new AGF();
        new RSSB();
        new BGA();
        new APR();
        new MCH();
        new IRSS();

        //supportive
        new SHR();
        new BTH();
        new WR();
        new SLD();
        new ASA();
        new ARS();

        //special
        new KOBW();
    }
}
