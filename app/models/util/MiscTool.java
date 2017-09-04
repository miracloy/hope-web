package models.util;

import models.dbtable.TaskTables;
import models.excel.BuyerTask;
import models.excel.ShopkeeperTask;
import models.excel.ShopkeeperTaskBook;
import models.excel.ShopkeeperTaskList;
import models.excel.BuyerTaskList;
import util.FileTool;
import util.LocalStoreTool;
import util.ZIPTool;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import models.BuyerManager;

/**
 * Created by weng on 15-12-10.
 */
public class MiscTool {

    public static byte[] buildDownloadBuyerZip(List<TaskTables> all){
        buildDownloadShuashouZip(all, "刷手.zip");
        try {
            return FileTool.getFileContent("刷手.zip");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            FileTool.delete("刷手.zip");
        }
    }

    public static byte[] buildDownloadShopkeeperZip(List<TaskTables> all){
        buildDownloadShopkeeperZip(all, "商家.zip");
        try {
            return FileTool.getFileContent("商家.zip");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            FileTool.delete("商家.zip");
        }
    }



    public static byte[] buildDownloadTaskZip(List<TaskTables> all){
        buildDownloadShuashouZip(all, "刷手.zip");
        buildDownloadShopkeeperZip(all, "商家.zip");
        String[] ss = new String[2];
        ss[0] = "刷手.zip";
        ss[1] = "商家.zip";
        ZIPTool.compressFiles2Zip(ss, "task.zip");
        try {
            byte[] ret = FileTool.getFileContent("task.zip");
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally {
            FileTool.delete("刷手.zip");
            FileTool.delete("商家.zip");
            FileTool.delete("task.zip");
        }
    }

    public static void buildDownloadShuashouZip(List<TaskTables> all,String zipname){
        List<models.dbtable.Buyer> ssl = BuyerManager.getALl();
        Map<String,Integer> levelMap = new HashMap();
        for(models.dbtable.Buyer buyer:ssl){
            levelMap.put(buyer.getWangwang(),buyer.getLevel());
        }
        Map<String,String> levelArrayMap = new HashMap();

        FileTool.deleteDirectory("exceltmp");
        FileTool.createDestDirectoryIfNotExists("exceltmp/");
        Map<Integer,String> idtobuyer = new HashMap<Integer,String>();
        Map<Integer,BuyerTaskList> stlmap = new HashMap<Integer,BuyerTaskList>();
        for(TaskTables task:all){
            idtobuyer.put(task.getBuyerTaskBookId(),task.getBuyerWangwang());
            Integer sbid = task.getBuyerTaskBookId();
            if(!stlmap.containsKey(sbid)){
                BuyerTaskList stl = new BuyerTaskList();
                stl.setFilepath("exceltmp/"+sbid+".xls");
                stl.setTaskbookid(sbid);
                stlmap.put(sbid,stl);
            }
            BuyerTaskList stl = stlmap.get(sbid);
            BuyerTask sst = new BuyerTask();
            sst.setShopname(task.getShopName());
            sst.setGoodnum(task.getGoodsNumber());
            sst.setUnit_price(task.getUnitPrice());
            sst.setAllPrice(task.getAllPrice());
            sst.setKeyword(task.getKeyword());
            String pic1 = task.getPic1();
            String pic2 = task.getPic2();
            String pic3 = task.getPic3();
            if(pic1!=null&&!pic1.equals("")){
                sst.addPic(LocalStoreTool.getImage(task.getTaskbookUuid()+pic1),pic1.split("\\.")[1]);
            }
            if(pic2!=null&&!pic2.equals("")){
                sst.addPic(LocalStoreTool.getImage(task.getTaskbookUuid()+pic2),pic2.split("\\.")[1]);
            }
            if(pic3!=null&&!pic3.equals("")){
                sst.addPic(LocalStoreTool.getImage(task.getTaskbookUuid()+pic3),pic3.split("\\.")[1]);
            }
            sst.setRequirement(task.getTaskRequirement());
            sst.setPhoneCost(task.getPhoneCost());
            stl.addShuashouTask(sst);

        }
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> names_realname = new ArrayList<String>();
        for (Map.Entry<Integer,BuyerTaskList> entry: stlmap.entrySet()) {
            names.add("exceltmp/"+entry.getKey()+".xls");
            BuyerTaskList sstl = (BuyerTaskList)entry.getValue();
            BigDecimal   b1   =   new BigDecimal(sstl.getZongBenJinNum());
            double   f1bj   =   b1.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();

            double benjin = +sstl.getZongBenJinNum() + sstl.getZongYongjinNum();
            BigDecimal   b2   =   new BigDecimal(benjin);
            double   f2benjin   =   b2.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();

            String real_n =  entry.getKey()+"号-共"+sstl.getZongDanShuNum()+"单-"+f1bj+
                    "+"+sstl.getZongYongjinNum()+"="+f2benjin+"元-"+idtobuyer.get(entry.getKey())+".xls";
            names_realname.add("exceltmp/"+real_n);
            levelArrayMap.put("exceltmp/"+real_n,""+levelMap.get(idtobuyer.get(entry.getKey())));
            try {
                ((BuyerTaskList)entry.getValue()).Deal();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(int i=0;i<names.size();i++){
            try {
                Files.move(Paths.get(names.get(i)),Paths.get(names_realname.get(i)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String,List<String>> levelArrayList = new HashMap();
        for(Map.Entry<String, String> entry : levelArrayMap.entrySet()){
            if(levelArrayList.get(entry.getValue()) == null){
                levelArrayList.put(entry.getValue(),new ArrayList<String>());
            }
            levelArrayList.get(entry.getValue()).add(entry.getKey());
        }
        List<String> lastarray = new ArrayList();
        for(Map.Entry<String, List<String>> entry : levelArrayList.entrySet()){
            List<String> tmpl = entry.getValue();
            ZIPTool.compressFiles2Zip(tmpl.toArray(new String[tmpl.size()]), entry.getKey()+".zip");
            lastarray.add(entry.getKey()+".zip");
        }

        ZIPTool.compressFiles2Zip(lastarray.toArray(new String[lastarray.size()]), zipname);
        for(String s:names_realname){
            FileTool.delete(s);
        }
        for(String s:lastarray){
            FileTool.delete(s);
        }
    }

    public static void buildDownloadShopkeeperZip(List<TaskTables> all,String zipname){
        FileTool.deleteDirectory("exceltmp");
        FileTool.createDestDirectoryIfNotExists("exceltmp/");
        Map<String,ShopkeeperTaskList> stbmap = new HashMap<String,ShopkeeperTaskList>();
        for(TaskTables task:all){
            String uuid = task.getTaskbookUuid()+task.getSubTaskbookId();
            ShopkeeperTask stk = new ShopkeeperTask();
            stk.initByTables(task);
            if(!stbmap.containsKey(uuid)){
                ShopkeeperTaskList stl = new ShopkeeperTaskList();
                stl.initByTask(stk);
                stbmap.put(uuid,stl);
            }
            ShopkeeperTaskList stl = stbmap.get(uuid);
            String pic = stk.getPic1();
            if(pic!=null &&!pic.equals("")){
                if(stl.getPic1() != null && !(stl.getPic1().equals(""))){
                    stl.setPic1(pic);
                }
            }
            pic = stk.getPic2();
            if(pic!=null &&!pic.equals("")){
                if(stl.getPic2() != null && !(stl.getPic2().equals(""))){
                    stl.setPic1(pic);
                }
            }
            pic = stk.getPic3();
            if(pic!=null &&!pic.equals("")){
                if(stl.getPic3() != null && !(stl.getPic3().equals(""))){
                    stl.setPic1(pic);
                }
            }
            stl.addShopkeeperTask(stk);
        }

        Map<String,ShopkeeperTaskBook> bookMap = new HashMap<String,ShopkeeperTaskBook>();
        for(Map.Entry<String,ShopkeeperTaskList> entry:stbmap.entrySet()){
            String uuid = entry.getValue().getTasklist().get(0).getTaskbookUuid();
            if(!bookMap.containsKey(uuid)){
                ShopkeeperTaskBook stb = new ShopkeeperTaskBook();
                stb.initByTask(entry.getValue());
                bookMap.put(uuid,stb);
            }
            ShopkeeperTaskBook stb = bookMap.get(uuid);
            stb.addShopkeeperTaskList(entry.getValue());
        }

        List<String> names = new ArrayList<String>();
        for(Map.Entry<String,ShopkeeperTaskBook> entry:bookMap.entrySet()){
            ShopkeeperTaskBook stb = entry.getValue();
            stb.generateExcel("exceltmp/"+stb.getTaskbookName());
            names.add("exceltmp/"+stb.getTaskbookName());
        }

        ZIPTool.compressFiles2Zip(names.toArray(new String[names.size()]), zipname);
        for(String s:names){
            FileTool.delete(s);
        }
    }
}
