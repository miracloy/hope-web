package models.excel;

import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import util.ExcelUtil;
import util.ImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shanmao on 15-12-10.
 */
@Data
public class ShopkeeperTaskList {

    private List<ShopkeeperTask> tasklist = new ArrayList<ShopkeeperTask>();
    private String shopkeeperName;
    private Map<String,byte[]> picContent = new HashMap<String,byte[]>();
    private Map<String,String> picType = new HashMap<String,String>();
    private Map<String,int[]> picSize = new HashMap<String,int[]>();
    private String shopName;
    private String shopWangwang;
    private String itemLink;
    private double pcCost;
    private double phoneCost;
    private String pic1 = "";
    private String pic2 = "";
    private String pic3 = "";
    private int subTaskBookid;

    //前端打包有些bug,需要手动添加get函数
    public List<ShopkeeperTask> getTasklist() {
        return tasklist;
    }

    public String getShopkeeperName() {
        return shopkeeperName;
    }

    public Map<String, byte[]> getPicContent() {
        return picContent;
    }

    public Map<String, String> getPicType() {
        return picType;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopWangwang() {
        return shopWangwang;
    }

    public String getItemLink() {
        return itemLink;
    }

    public double getPcCost() {
        return pcCost;
    }

    public double getPhoneCost() {
        return phoneCost;
    }

    public String getPic1() {
        return pic1;
    }

    public String getPic2() {
        return pic2;
    }

    public String getPic3() {
        return pic3;
    }


    public int getTaskNum(){
        int ret = 0;
        for(ShopkeeperTask task:tasklist){
            ret += 1;
        }
        return ret;
    }

    public double getTaskAllPriceSum(){
        double ret = 0;
        for(ShopkeeperTask task:tasklist){
            ret += task.getAllPrice();
        }
        return ret;
    }

    public int parse(HSSFSheet sheet,int start,int end,String taskBookUuid,String taskBookName,
                     Map<String,String> picMap,Integer idIndex,int subtaskbookid) throws Exception {

        shopkeeperName = ExcelUtil.getCellString(sheet,start,1);
        shopName  = ExcelUtil.getCellString(sheet,start,3);
        shopWangwang = ExcelUtil.getCellString(sheet,start,5);
        itemLink = ExcelUtil.getCellString(sheet,start+1,1);

        //读取商品pc端价格
        try {
            pcCost = ExcelUtil.getCellDouble(sheet, start + 2, 1);
        } catch (Exception e) {
            throw new Exception("单元格" + ExcelUtil.getUnitNo(start + 2, 1));
        }

        //读取商品移动端价格
        try {
            phoneCost = ExcelUtil.getCellDouble(sheet, start + 3, 1);
        } catch (Exception e) {
            throw new Exception("单元格" + ExcelUtil.getUnitNo(start + 3, 1));
        }

        String pic = ExcelUtil.getCellString(sheet,start+4,1);
        pic1 = picMap.get(pic);
        pic = ExcelUtil.getCellString(sheet,start+5,1);
        pic2 = picMap.get(pic);
        pic = ExcelUtil.getCellString(sheet,start+6,1);
        pic3 = picMap.get(pic);
        this.subTaskBookid = subtaskbookid;


        //遍历读取这个商品的所有任务
        int index = 0;
        for(int i=start+8;i<=end;i++){
            if(!ExcelUtil.hasRow(sheet,i)){
                continue;
            }

            try {
                if (ExcelUtil.getCellDouble(sheet, i, 4) == null) {
                    continue;
                }
            } catch (Exception e) {
                throw new Exception("单元格" + ExcelUtil.getUnitNo(i, 4));
            }

            index++;
            ShopkeeperTask st = new ShopkeeperTask();
            st.setId(index+idIndex);
            st.setKeyword(ExcelUtil.getCellString(sheet, i, 1));
            st.setTaskRequirement(ExcelUtil.getCellString(sheet, i, 2));

            //读取任务单价
            try {
                st.setUnitPrice(ExcelUtil.getCellDouble(sheet, i, 3));
            } catch (Exception e) {
                throw new Exception("单元格" + ExcelUtil.getUnitNo(i, 3));
            }

            //读取商品数量
            try {
                st.setGoodsNumber(ExcelUtil.getCellInt(sheet, i, 4));
            } catch (Exception e) {
                throw new Exception("单元格" + ExcelUtil.getUnitNo(i, 4));
            }

            if(ExcelUtil.getCellDouble(sheet, i, 5) != null){
                //读取任务总价
                try {
                    st.setAllPrice(ExcelUtil.getCellDouble(sheet, i, 5));
                } catch (Exception e) {
                    throw new Exception("单元格" + ExcelUtil.getUnitNo(i, 5));
                }
            } else {
                //计算任务总价
                Cell cell =  sheet.getRow(i).getCell(5);
                if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
                    st.setAllPrice(cell.getNumericCellValue());
                } else {
                    st.setAllPrice(ExcelUtil.getCellDouble(sheet, i, 3)*ExcelUtil.getCellDouble(sheet, i, 4));
                }
            }


            st.setTaskBookUuid(taskBookUuid);
            st.setTaskBookName(taskBookName);
            st.setShopkeeperName(shopkeeperName);
            st.setShopName(shopName);
            st.setShopWangwang(shopWangwang);
            st.setItemLink(itemLink);
            st.setPcCost(pcCost);
            st.setPhoneCost(phoneCost);
            st.setPic1(pic1);
            st.setPic2(pic2);
            st.setPic3(pic3);
            st.setSubTaskBookId(subtaskbookid);
            tasklist.add(st);
        }

        return index;
    }


    public int generateExcel(Sheet sheet,int top,Map<String,Integer> allline,Map<String,byte[]> picContentMap){
        int prelinenum = allline.get("allline");
        ExcelUtil.getOrCreateCell(sheet,top+0,0).setCellValue("商家姓名");
        ExcelUtil.getOrCreateCell(sheet,top+0,1).setCellValue(shopkeeperName);
        ExcelUtil.getOrCreateCell(sheet,top+0,2).setCellValue("店铺名称");
        ExcelUtil.getOrCreateCell(sheet,top+0,3).setCellValue(shopName);
        ExcelUtil.getOrCreateCell(sheet,top+0,4).setCellValue("店铺旺旺");
        ExcelUtil.getOrCreateCell(sheet,top+0,5).setCellValue(shopWangwang);
        ExcelUtil.getOrCreateCell(sheet,top+1,0).setCellValue("产品链接");
        CellStyle hlink_style = sheet.getWorkbook().createCellStyle();
        Font hlink_font = sheet.getWorkbook().createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);

        if(itemLink != null){
            Cell cell = ExcelUtil.getOrCreateCell(sheet, top+1, 1);
            cell.setCellValue(itemLink);
            CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
            Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
            link.setAddress(itemLink);
            cell.setHyperlink(link);
            cell.setCellStyle(hlink_style);
        }

        ExcelUtil.getOrCreateCell(sheet,top+2,0).setCellValue("电脑端价格");
        ExcelUtil.getOrCreateCell(sheet,top+2,1).setCellValue(pcCost);
        ExcelUtil.getOrCreateCell(sheet,top+3,0).setCellValue("手机端价格");
        ExcelUtil.getOrCreateCell(sheet,top+3,1).setCellValue(phoneCost);

        ExcelUtil.getOrCreateCell(sheet,top+7,0).setCellValue("序号");
        ExcelUtil.getOrCreateCell(sheet,top+7,1).setCellValue("关键词（手机淘宝搜索）");
        ExcelUtil.getOrCreateCell(sheet,top+7,2).setCellValue("任务要求");
        ExcelUtil.getOrCreateCell(sheet,top+7,3).setCellValue("单价");
        ExcelUtil.getOrCreateCell(sheet,top+7,4).setCellValue("数量");
        ExcelUtil.getOrCreateCell(sheet,top+7,5).setCellValue("总价");
        ExcelUtil.getOrCreateCell(sheet,top+7,6).setCellValue("刷手旺旺账号");
        ExcelUtil.getOrCreateCell(sheet,top+7,7).setCellValue("任务书");
        ExcelUtil.getOrCreateCell(sheet,top+7,8).setCellValue("刷书分组");
        for(ShopkeeperTask st:tasklist){
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,0).setCellValue(st.getId());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,1).setCellValue(st.getKeyword());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,2).setCellValue(st.getTaskRequirement());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,3).setCellValue(st.getUnitPrice());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,4).setCellValue(st.getGoodsNumber());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,5).setCellValue(st.getAllPrice());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,6).setCellValue(st.getBuyerWangwang());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,7).setCellValue(st.getBuyerTaskBookId());
            ExcelUtil.getOrCreateCell(sheet,top+7+st.getId()-prelinenum,8).setCellValue(st.getBuyerTeam());
        }


        //放图片
        if(pic1!=null&&!pic1.equals("")){
            picContent.put(pic1,picContentMap.get(pic1));
        }
        if(pic2!=null&&!pic2.equals("")){
            picContent.put(pic2,picContentMap.get(pic2));
        }
        if(pic3!=null&&!pic3.equals("")){
            picContent.put(pic3,picContentMap.get(pic3));
        }

        float h = ExcelUtil.getOrCreateRow(sheet,8).getHeightInPoints();
        float w = sheet.getColumnWidthInPixels(0);
        float radio = w/h;

        int allpicw = 0;
        int allpich = 0;
        for(Map.Entry<String,byte[]> entry:picContent.entrySet()){
            byte[] content = entry.getValue();
            int[] tmpr = new int[0];
            try {
                tmpr = ImageUtil.getImageSize(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            allpicw += tmpr[0];
            if(allpich<tmpr[1]){
                allpich = tmpr[1];
            }
            picSize.put(entry.getKey(),tmpr);
        }

        int numberwide=0;
        int numberwideall = 4;
        for(Map.Entry<String,byte[]> entry:picContent.entrySet()){
            int ww =  picSize.get(entry.getKey())[0];
            int hh =  picSize.get(entry.getKey())[1];
            float picradio = 1;
            if(allpicw>allpich){
                picradio = ((float)(ww))/allpicw;
            }
            int pictureIdx = sheet.getWorkbook().addPicture(entry.getValue(), ExcelUtil.getPicTypeByString(entry.getKey().split("\\.")[1]));
            CreationHelper helper = sheet.getWorkbook().getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            if(allpicw>allpich){
                anchor.setCol1(10+numberwide);
                numberwide += (int)(numberwideall*picradio)+1;
            }else{
                anchor.setCol1(10+numberwide);
                numberwide += (int)(numberwideall*ww/hh/radio*2.5)+1;
            }
            anchor.setRow1(2+top);
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            if(allpicw>allpich){
                pict.resize(numberwideall*picradio,numberwideall*radio*hh/ww*picradio);
            }else{
                pict.resize(numberwideall*ww/hh/radio*2.5,numberwideall*2.5);
            }
        }
        allline.put("allline",allline.get("allline")+tasklist.size());
        return 7+tasklist.size()+4;
    }


    public void initByTask(ShopkeeperTask task){
        shopkeeperName = task.shopkeeperName;
        shopName = task.shopName;
        shopWangwang = task.shopWangwang;
        itemLink = task.getItemLink();
        pcCost = task.getPcCost();
        phoneCost = task.phoneCost;
        pic1 = task.pic1;
        pic2 = task.pic2;
        pic3 = task.pic3;
        subTaskBookid = task.getSubTaskBookId();
    }

    public void addShopkeeperTask(ShopkeeperTask st){
        tasklist.add(st);
    }
}