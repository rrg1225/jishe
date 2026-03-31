package com.example.student.university_career_platform.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 企业级内容安全：基于强类型 Trie 字典树的 DFA 毫秒级敏感词过滤算法
 */
public class DFAFilterUtil {

    // 🌟 核心优化：定义强类型的字典树节点，彻底告别 Map<Object, Object> 和 unchecked 警告
    private static class TrieNode {
        boolean isEnd = false; // 是否是敏感词的结尾
        Map<Character, TrieNode> children = new HashMap<>();
    }

    private static final TrieNode rootNode = new TrieNode();

    // 静态代码块，系统启动时加载敏感词库
    static {
        Set<String> words = new HashSet<>();
        words.add("代考");
        words.add("作弊");
        words.add("外挂");
        words.add("买卖论文");
        words.add("刷单");
        words.add("办假证");
        initKeyWord(words);
    }

    /**
     * 构建强类型多叉树
     */
    private static void initKeyWord(Set<String> keyWordSet) {
        for (String word : keyWordSet) {
            TrieNode currentNode = rootNode;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                // 如果没有这个字符的分支，就创建一个新节点
                currentNode.children.putIfAbsent(c, new TrieNode());
                // 顺着树往下走
                currentNode = currentNode.children.get(c);
            }
            // 单词遍历完成，将最后一个节点标记为结尾
            currentNode.isEnd = true;
        }
    }

    /**
     * 将文本中的敏感词替换为星号 *** (优化了字符串拼接性能)
     */
    public static String replaceSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 使用 StringBuilder 提升高并发下的字符串修改性能
        StringBuilder resultText = new StringBuilder(text);

        for (int i = 0; i < resultText.length(); i++) {
            int matchLength = checkSensitiveWord(resultText.toString(), i);
            if (matchLength > 0) {
                // 将匹配到的长度全部替换为 *
                for (int j = 0; j < matchLength; j++) {
                    resultText.setCharAt(i + j, '*');
                }
                i = i + matchLength - 1; // 极速跳跃
            }
        }
        return resultText.toString();
    }

    /**
     * 强类型 DFA 嗅探器
     * @return 匹配到的敏感词长度，没匹配到返回0
     */
    private static int checkSensitiveWord(String txt, int beginIndex) {
        boolean flag = false;
        int matchFlag = 0;
        TrieNode currentNode = rootNode;

        for (int i = beginIndex; i < txt.length(); i++) {
            char word = txt.charAt(i);
            currentNode = currentNode.children.get(word);

            if (currentNode != null) {
                matchFlag++;
                if (currentNode.isEnd) {
                    flag = true;
                    break; // 最短匹配原则，查到即止
                }
            } else {
                break; // 树枝断裂，不是敏感词
            }
        }
        return flag ? matchFlag : 0;
    }
}