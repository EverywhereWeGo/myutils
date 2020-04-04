package tools.pinying;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static utils.basicutil.i_StringUtil.deleteChar;

public class DuoYinZi {
    private static Map<String, List<String>> pinyinMap = new HashMap<String, List<String>>();

    static {
        try {
            String filePath = "/Users/everywherewego/IdeaProjects/myutils/src/main/java/tools/pinying/dict.txt";
            InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String s = null;
            try {
                while ((s = br.readLine()) != null) {
                    if (s != null) {
                        String[] arr = s.split("=");
                        String pinyin = arr[0].trim();
                        String chinese = arr[1];
                        if (chinese != null) {
                            String[] strs = chinese.split(",");
                            for (int i = 0; i < strs.length; i++) {
                                strs[i] = strs[i].trim();
                            }
                            List<String> list = Arrays.asList(strs);
                            pinyinMap.put(pinyin, list);

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static String getPinYin(String hanyu, String charset) {
        List<Tuple> pinying = transformToPinYin(hanyu);
        String filed = concatString(pinying, charset);
        return filed;
    }

    public static String getPinYin(String hanyu) {
        List<Tuple> pinying = transformToPinYin(hanyu);
        String filed = concatString(pinying, "");
        return filed;
    }


    public static String getFirstPinYin(String hanyu) {
        StringBuffer sb = new StringBuffer();
        List<Tuple> tuples = transformToPinYin(hanyu);

        for (Tuple p : tuples) {
            if (p.isPinYing()) {
                sb.append(p.getContext().charAt(0));
            } else {
                sb.append(p.getContext());
            }
        }
        return sb.toString();
    }


    //对拼音进行处理，比如转为驼峰或者加下划线
    private static String concatString(List<Tuple> tuples, String charset) {
        StringBuffer sb = new StringBuffer();
        for (Tuple p : tuples) {
            if (p.isPinYing()) {
                sb.append(p.getContext()).append(charset);
            } else {
                deleteChar(sb, charset).append(p.getContext());
            }
        }
        return sb.toString();
    }

    private static List<Tuple> transformToPinYin(String chinese) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        //把字符串按照中文和其他字符进行切割
        List<String> fields = new LinkedList<>();
        for (int i = 0; i < chinese.length(); i++) {
            String t = chinese.substring(i, i + 1);
            //是中文的话新增
            if (t.matches("[\\u4E00-\\u9FA5]+")) {
                fields.add(t);
                //不是中文的话,判断列表中最后一个是否是中文,是的话,新增,不是的话,修改内容
            } else {
                if (fields.size() == 0 || fields.get(fields.size() - 1).matches("[\\u4E00-\\u9FA5]+")) {
                    fields.add(t);
                } else {
                    fields.set(fields.size() - 1, fields.get(fields.size() - 1) + t);
                }
            }
        }


        //存放拼音
        List<Tuple> result = new LinkedList<>();

        //开始遍历语句中的每一个汉字
        for (int i = 0; i < fields.size(); i++) {
            Tuple tuple = new Tuple();
            String ch = fields.get(i);
            //如果是汉字
            if (ch.length() == 1 && ch.matches("[\\u4E00-\\u9FA5]+")) {
                tuple.setPinYing(true);
                String[] pys = new String[0];
                try {
                    pys = PinyinHelper.toHanyuPinyinStringArray(ch.charAt(0), format);
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                }
                //去重,去除音调不同的
                List<String> list = new LinkedList<String>();
                for (String py : pys) {
                    if (!list.contains(py)) {
                        list.add(py);
                    }
                }

                if (list.size() == 1) {
                    tuple.setContext(list.get(0));
                } else {
                    //遍历该字的每一个读音
                    boolean flag = false;
                    for (String s : list) {
                        if (pinyinMap.containsKey(s)) {
                            List<String> phrase = pinyinMap.get(s);
                            //连上后一个字
                            if (i + 1 < fields.size()) {
                                String concat = ch + fields.get(i + 1);
                                if (phrase.contains(concat)) {
                                    tuple.setContext(s);
                                    flag = true;
                                    break;
                                }
                            }
                            //连上前一个字
                            if (0 <= i - 1) {
                                String concat = fields.get(i - 1) + ch;
                                if (phrase.contains(concat)) {
                                    tuple.setContext(s);
                                    flag = true;
                                    break;
                                }
                            }
                            //单字被包含,表示单字时的常用读音,如"否"
                            if (phrase.contains(ch)) {
                                tuple.setContext(s);
                                flag = true;
                                break;
                            }
                        }
                    }
                    //如果遍历所有的读音都没有,则直接返回原字符
                    if (!flag) {
                        tuple.setContext(ch);
                    }
                }
            } else {
                //如果不是汉字,直接返回原字符
                tuple.setPinYing(false);
                tuple.setContext(ch);
            }
            result.add(tuple);
        }
        return result;
    }

    static class Tuple {
        private boolean isPinYing;
        private String context;

        public boolean isPinYing() {
            return isPinYing;
        }

        public void setPinYing(boolean pinYing) {
            isPinYing = pinYing;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        @Override
        public String toString() {
            return "Tuple{" +
                    "isPinYing=" + isPinYing +
                    ", context='" + context + '\'' +
                    '}';
        }
    }


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, BadHanyuPinyinOutputFormatCombination {
        System.out.println(getPinYin("排污单位名称 所在省 所在市 所在区县 行业类别 提交时间 审批节点 提交审批机关级别 办理类型 元数据 metadata_itemtypeid", "_"));
        System.out.println(getFirstPinYin("a_首页_asdf_许可证业务审核_申请审核"));

//        System.out.println(Arrays.toString(getPinYin("否")));

//        System.out.println(Arrays.toString(getPinYin("单于夜遁逃")));

    }


}