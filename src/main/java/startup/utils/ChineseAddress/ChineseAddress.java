package startup.utils.ChineseAddress;

import java.util.List;

/**
 * 中国地址的容器
 */
public class ChineseAddress {
    /**
     * 源地址
     */
    private String source;
    /**
     * 国家
     */
    private String nation;
    /**
     * 省份
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 县
     */
    private String county;
    /**
     * 区
     */
    private String district;

    /**
     * 源地址
     */
    public String getSource() {
        return source;
    }

    /**
     * 源地址
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 省份,包含：省|特区|自治区|特别行政区
     */
    public String getProvince() {
        return province;
    }

    /**
     * 省份,包含：省|特区|自治区|特别行政区
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 市 包含：市|地区|自治州
     */
    public String getCity() {
        return city;
    }

    /**
     * 市 包含：市|地区|自治州
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 县 包含：乡|县
     */
    public String getCounty() {
        return county;
    }

    /**
     * 县 包含：乡|县
     */
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     * 区
     */
    public String getDistrict() {
        return district;
    }

    /**
     * 区
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * 街道
     */
    public String getStreet() {
        return street;
    }

    /**
     * 街道
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * 街道
     */
    public String street;
    /**
     * 胡同|弄堂|街|巷|路|道
     */
    public List<String> roads;
    public String number;
    public String plaza;
    public String ip;
    public String town;
    public String village;
    public String zone;
    public String underground;
    public List<String> notes;
    public List<String> noises;
    private static final String SEPARATOR = System.getProperty("line.separator");

    public String toString() {
        String s = "src: " + source + SEPARATOR;
        if (nation != null) {
            s = s + "nat: " + nation + SEPARATOR;
        }
        if (province != null) {
            s = s + "pro: " + province + SEPARATOR;
        }
        if (city != null) {
            s = s + "cit: " + city + SEPARATOR;
        }
        if (county != null) {
            s = s + "cou: " + county + SEPARATOR;
        }
        if (district != null) {
            s = s + "dis: " + district + SEPARATOR;
        }
        if (street != null) {
            s = s + "str: " + street + SEPARATOR;
        }
        if (number != null) {
            s = s + "num: " + number + SEPARATOR;
        }
        if (plaza != null) {
            s = s + "pla: " + plaza + SEPARATOR;
        }
        if (ip != null) {
            s = s + "idp: " + ip + SEPARATOR;
        }
        if (town != null) {
            s = s + "twn: " + town + SEPARATOR;
        }
        if (village != null) {
            s = s + "vil: " + village + SEPARATOR;
        }
        if (zone != null) {
            s = s + "zon: " + zone + SEPARATOR;
        }
        if (underground != null) {
            s = s + "udg: " + underground + SEPARATOR;
        }
        if (roads != null) {
            s = s + "rod: ";
            for (int i = 0; i < roads.size(); i++) {
                String r = roads.get(i);
                if (r == roads.get(0)) {
                    s = s + r;
                } else {
                    s = s + " / " + r;
                }
            }
            s = s + SEPARATOR;
        }
        if (notes != null) {
            s = s + "not: ";
            for (int i = 0; i < notes.size(); i++) {
                String n = notes.get(i);
                if (n == roads.get(0)) {
                    s = s + n;
                } else {
                    s = s + " / " + n;
                }
            }
            s = s + SEPARATOR;
        }
        if (noises != null) {
            s = s + "noi: ";
            for (int i = 0; i < noises.size(); i++) {
                s = s + noises.get(i) + " / ";
            }
            s = s + SEPARATOR;
        }
        return s;
    }

}
