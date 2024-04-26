package top.melody.cloud.game.bloom.reptiles;

import jakarta.annotation.Resource;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import top.melody.cloud.game.bloom.dal.dataobject.RoleDictionaryDO;
import top.melody.cloud.game.bloom.service.RoleDictionaryService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
@Data
public class HttpXxlJob {
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Resource
    private RoleDictionaryService roleDictionaryService;

    public void httpJobHandler(Map<String,Integer> deleteMap, List<String> todoUpdate, String... titles) throws ExecutionException, InterruptedException, IOException {
        final String night1 = "night1";
        final String dawn1 = "dawn1";
        final String night2 = "night2";
        final String dawn2 = "dawn2";
        List<CompletableFuture<List<RoleDictionaryDO>>> futures = new ArrayList<>();

        CompletableFuture<Map<String, Integer>> orderFuture = new CompletableFuture<>();

        for (String title : titles) {
            try {
                // 获取HTML内容
                String url = "https://clocktower-wiki.gstonegames.com/index.php?title=%s".formatted(title);
                String htmlContent = fetchHtmlContent(url);
                if (title.equals("%E5%A4%9C%E6%99%9A%E8%A1%8C%E5%8A%A8%E9%A1%BA%E5%BA%8F%E4%B8%80%E8%A7%88")) {
                    orderFuture.complete(parseAndFilterRoleOrder(htmlContent));
                } else {
                    CompletableFuture<List<RoleDictionaryDO>> future = CompletableFuture.supplyAsync(() -> {
                        logger.info("【" + title + "】数据爬取开始");
                        // 解析HTML并筛选元素
                        List<RoleDictionaryDO> roleDictionaryDOS = parseAndFilterRole(htmlContent, title);
                        if (deleteMap.containsKey(title)) {
                            for (int i = 0; i < deleteMap.get(title); i++) {
                                roleDictionaryDOS.removeFirst();
                            }
                        }
                        return roleDictionaryDOS;
                    });
                    futures.add(future);
                }
            } catch (Exception e) {
                logger.throwing(this.getClass().getName(), "httpJobHandler", e);
            }
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<RoleDictionaryDO>> allRolesFuture = allOf.thenApply(v-> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(ArrayList::new, List::add, List::addAll)
        );
        
        List<RoleDictionaryDO> allRoles = allRolesFuture.get();
        CompletableFuture.allOf(new CompletableFuture[]{orderFuture}).join();
        Map<String, Integer> stringIntegerMap = orderFuture.get();
        // 寻找目标标签并放入map
        for (RoleDictionaryDO allRole : allRoles) {
            if (stringIntegerMap.containsKey(night1 + "_" + allRole.getRoleName())) {
                allRole.setRoleFirstNightOrder(Long.valueOf(stringIntegerMap.get(night1 + "_" + allRole.getRoleName())));
            }
            if (stringIntegerMap.containsKey(dawn1 + "_" + allRole.getRoleName())) {
                allRole.setRoleFirstNightOrder(Long.valueOf(stringIntegerMap.get(dawn1 + "_" + allRole.getRoleName())));
            }
            if (stringIntegerMap.containsKey(night2 + "_" + allRole.getRoleName())) {
                allRole.setRoleOtherNightOrder(Long.valueOf(stringIntegerMap.get(night2 + "_" + allRole.getRoleName())));
            }
            if (stringIntegerMap.containsKey(dawn2 + "_" + allRole.getRoleName())) {
                allRole.setRoleOtherNightOrder(Long.valueOf(stringIntegerMap.get(dawn2 + "_" + allRole.getRoleName())));
            }
        }
        roleDictionaryService.batchAddOrUpdate(allRoles);
        logger.info("全部数据存储完成");
    }

    // 从指定URL获取HTML内容
    private String fetchHtmlContent(String url) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            URL realUrl = new URI(url).toURL();
            connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Http Request StatusCode(" + connection.getResponseCode() + ") Invalid.");
            }

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) result.append(line);
            return result.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
                connection.disconnect();
            } else {
                if (connection != null) connection.disconnect();
            }
        }
    }

    private Map<String, Integer> parseAndFilterRoleOrder(String htmlContent) {
        Map<String, Integer> tagIndexMap = new HashMap<>();
        try {
            Document doc = Jsoup.parse(htmlContent);
            Element parserOutputDiv = doc.select("div.mw-parser-output").first();

            if (parserOutputDiv != null) {
                Elements bTags = parserOutputDiv.select("b");
                // 获取parserOutputDiv里面所有的b标签

                // 寻找目标标签并放入map
                Map<String, Integer> andMapTags = findAndMapTags(bTags);
                tagIndexMap.putAll(andMapTags);
                return tagIndexMap;
            }
        } catch (Exception e) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String methodName = stackTrace[1].getMethodName();
            logger.throwing(this.getClass().getName(), methodName, e);
        }
        return tagIndexMap;
    }

    private Map<String, Integer> findAndMapTags(Elements tags) {
        Map<String, Integer> tagIndexMap = new HashMap<>();
        int newIndex = 1;
        int matchTime = 0;
        List<Node> aList = new ArrayList<>();
        List<Node> bList = new ArrayList<>();

        for (Element tag : tags) {
            String text = tag.text();

            // Check for the first occurrence of the keyword
            if (matchTime == 0 && "黄昏".equals(text)) {
                processTag(tag, tagIndexMap, "night1", aList, newIndex);
                matchTime++;
            } else if (matchTime == 1 && !"黄昏".equals(text)) {
                tagIndexMap.put("night1_" + text, newIndex++);
            } else if (matchTime == 1) {
                newIndex = 1;
                processTag(tag, tagIndexMap, "night2", bList, newIndex);
                matchTime++;
            } else if (matchTime == 2 && !"黄昏".equals(text)) {
                tagIndexMap.put("night2_" + text, newIndex++);
            }
        }
        return tagIndexMap;
    }

    private void processTag(Element tag, Map<String, Integer> tagIndexMap, String keyPrefix, List<Node> nodeList, int newIndex) {
        // Handle the common logic for processing a tag
        List<Node> traveller = Objects.requireNonNull(tag.parentNode()).childNodes().stream()
                .filter(child -> !child.toString().contains("<img") && child.toString().contains("<a"))
                .toList();
        nodeList.addAll(traveller);
        for (Node node : nodeList) {
            String s = node.childNodes().getFirst().toString();
            tagIndexMap.put(keyPrefix + "_" + s, newIndex++);
        }
    }


    private List<RoleDictionaryDO> parseAndFilterRole(String htmlContent, String title) {
        List<RoleDictionaryDO> roleDictionaries = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(htmlContent);
            Elements galleryTextDivs = doc.select("div.gallerytext");
            Elements thumbDivs = doc.select("div.thumb");

            List<CompletableFuture<RoleDictionaryDO>> futures = new ArrayList<>();
            for (int i = 0; i < galleryTextDivs.size(); i++) {
                Element galleryTextDiv = galleryTextDivs.get(i);
                Elements aTags = galleryTextDiv.select("a");

                String roleIcon = "https://clocktower-wiki.gstonegames.com";
                Element thumbDiv = thumbDivs.get(i);
                if (!thumbDiv.children().isEmpty()) {
                    Element imgElement = thumbDiv.select("img").first();
                    if (imgElement != null) roleIcon += imgElement.attr("src");
                }

                for (Element aTag : aTags) {
                    String roleTitle = aTag.attr("title");
                    String finalRoleIcon = roleIcon;
                    CompletableFuture<RoleDictionaryDO> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            String titleContent = fetchHtmlContent("https://clocktower-wiki.gstonegames.com/index.php?title=" + roleTitle);
                            return createRoleDictionaryModel(roleTitle, titleContent, finalRoleIcon);
                        } catch (IOException e) {
                            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                            String methodName = stackTrace[1].getMethodName();
                            logger.throwing(this.getClass().getName(), methodName, e);
                            return null;
                        }
                    });
                    futures.add(future);
                }
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.join();
            for (CompletableFuture<RoleDictionaryDO> future : futures) {
                roleDictionaries.add(future.join());
            }
        } catch (Exception e) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String methodName = stackTrace[1].getMethodName();
            logger.throwing(this.getClass().getName(), methodName, e);
        }
        logger.info("【" + title + "】数据爬取完成");
        return roleDictionaries;
    }

    private RoleDictionaryDO createRoleDictionaryModel(String roleTitle, String titleContent, String
            finalRoleIcon) {
        String roleBackgroundStory = extractContentBetweenIds(titleContent, "背景故事", "角色能力");
        String roleAbility = extractContentBetweenIds(titleContent, "角色能力", "角色简介");
        String roleIntroduction = extractContentBetweenIds(titleContent, "角色简介", "范例");
        String roleExamples = extractContentBetweenIds(titleContent, "范例", "运作方式");
        String roleOperationMode = extractContentBetweenIds(titleContent, "运作方式", "提示标记");
        String rolePromptImprint = extractContentBetweenIds(titleContent, "提示标记", "规则细节");
        String roleTipsAndTechniques = extractContentBetweenIds(titleContent, "提示与技巧", "伪装成" + roleTitle);
        String roleCamouflageTechniques = extractContentBetweenIds(titleContent, "伪装成" + roleTitle, "角色信息");
        String roleRuleDetails = extractContentBetweenIds(titleContent, "规则细节", "提示与技巧");
        String roleType = extractRoleInfo(titleContent, "<li>角色类型：(.*?)</li>");
        // 假设title是角色的标题
        // 假设title是角色的标题
        String roleCamp = switch (roleType) {
            case "镇民", "外来者" -> "善良";
            case "爪牙", "恶魔" -> "邪恶";
            default -> "未知阵营";
        };
        return RoleDictionaryDO.builder()
                .roleIcon(finalRoleIcon)
                .roleName(roleTitle)
                .roleBackgroundStory(roleBackgroundStory)
                .roleAbility(roleAbility)
                .roleExample(roleExamples)
                .roleOperationMode(roleOperationMode)
                .rolePromptImprint(rolePromptImprint)
                .roleRuleDetails(roleRuleDetails)
                .roleId(extractRoleInfo(titleContent, "<li>英文名：(.*?)</li>"))
                .roleIntroduction(roleIntroduction)
                .roleTipsAndTechniques(roleTipsAndTechniques)
                .roleCamouflageTechniques(roleCamouflageTechniques)
                .roleType(roleType)
                .collectionOfBelongingRole(extractRoleInfo(titleContent, "<li>所属剧本：(.*?)</li>"))
                .roleCamp(roleCamp)
                .creativeFrom(extractRoleInfo(titleContent, "<li>创意来源：(.*?)</li>"))
                .build();
    }

    private String extractContentBetweenIds(String htmlContent, String startId, String endId) {
        // 构建正则表达式，匹配指定ID之间的内容
        // startId = 背景故事 endId = 角色能力
        String regex = "id=\"" + startId + "\".*id=\"" + endId + "\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            String matchedContent = htmlContent.substring(matcher.start(), matcher.end());

            // marchedContent = "id="背景故事">背景故事</span></h2><p><i>“晚礼服上的血渍？</i></p><p><i>才不是呢，这只是洒上了雪莉酒。真粗心！”</i></p><h2><span id=".E8.A7.92.E8.89.B2.E8.83.BD.E5.8A.9B"></span><span class="mw-headline" id="角色能力""
            // 使用正则表达式去除所有标签及标签的属性内容
            String filteredContent = matchedContent.replaceAll("<[^>]*>|" + startId + "|" + endId, "").replaceAll("id.*>|<.*\"", "");

            // 返回过滤后的内容
            return filteredContent.trim();
        } else {
            // 如果未找到匹配的内容，返回空字符串或者其他适当的值
            return "";
        }
    }

    private String extractRoleInfo(String htmlContent, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            // 使用group(0)获取匹配的内容
            String matchedContent = htmlContent.substring(matcher.start(), matcher.end());

            // 使用正则表达式去除所有标签及标签的属性内容
            String filteredContent = matchedContent.replaceAll("<[^>]*>|英文名：|所属剧本：|角色类型：|角色能力类型：|创意来源：", "");

            // 返回过滤后的内容
            return filteredContent.trim();
        } else {
            // 如果未找到匹配的内容，返回空字符串或者其他适当的值
            return "";
        }
    }
}
