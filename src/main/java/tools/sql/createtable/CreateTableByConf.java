package tools.sql.createtable;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.List;

import static tools.fileReadbyLine.readLine;
import static tools.pinying.DuoYinZi.getFirstPinYin;
import static tools.pinying.DuoYinZi.getPinYin;
import static utils.basicutil.f_SqlUtil.ddlSql;

public class CreateTableByConf {
    public static void main(String[] args) throws BadHanyuPinyinOutputFormatCombination {
        List<String> eachLine = readLine("/Users/everywherewego/IdeaProjects/myutils/src/main/java/tools/sql/createtable/aaa.txt");

        String drop = "DROP TABLE IF EXISTS `sys_paiwuxuke_{0}`";
        String modelSql = "CREATE TABLE IF NOT EXISTS `sys_paiwuxuke_{0}` (\n" +
                "`id` BIGINT ( 20 ) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增id',\n" +
                "`etl_date` VARCHAR ( 255 ) DEFAULT NULL COMMENT '入库时间',\n" +
                "{1}" +
                "PRIMARY KEY ( `id` ) \n" +
                ") ENGINE = INNODB " +
                "AUTO_INCREMENT = 1 " +
                "DEFAULT CHARSET = utf8mb4 " +
                "comment='{2}'";
        //每一行
        for (String line : eachLine) {
            //每一行针对空格分隔
            if (line.startsWith("#")) {
                continue;
            }
            String tablename = line.split("=")[0].trim();
            String fieldname = line.split("=")[1].trim();
            //表名
            String tn = getFirstPinYin(tablename);
            String dropTableSql = drop.replace("{0}", tn);
            System.out.println(dropTableSql);


            //字段名
            StringBuffer sb = new StringBuffer();
            String[] sp = fieldname.split(" ");
            for (String obj : sp) {
                String filed = getPinYin(obj, "_");
                String b = createFieldInfo(filed, obj);
                sb.append(b);
            }
            String createTableSql = modelSql.replace("{0}", tn).replace("{1}", sb.toString()).replace("{2}", tablename);
            System.out.println(createTableSql);


            ddlSql(dropTableSql);
            ddlSql(createTableSql);
        }
    }


    public static String createFieldInfo(String fileds, String comment) {
        String modelFile = "`{0}` VARCHAR ( 255 ) DEFAULT NULL COMMENT '{1}',\n";
        return modelFile.replace("{0}", fileds).replace("{1}", comment);

    }
}
