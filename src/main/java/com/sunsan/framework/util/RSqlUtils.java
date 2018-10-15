package com.sunsan.framework.util;


import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.*;
import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RSqlUtils {

    private static String url="jdbc:mysql://47.105.69.13:9101/db_dz_hzz";
    private static String username="dev";
    private static String password = "Dzhzz12#$";

    private static Logger logger = LoggerFactory.getLogger(RSqlUtils.class);

    /**
     * 排序字符串参数转换为sort
     *
     * @param sortString "userName desc;createTime asc;userMobile;" 默认desc
     * @return Sort
     */
    public static Query getSortByString(Query query, String sortString) {
        if (StringUtils.isEmpty(sortString))
            return query;
        String[] sortList = StringUtils.split(sortString, ";");
        for (String temp : sortList) {
            String[] sortAttr = StringUtils.split(temp, " ");
            boolean bDesc = true;
            if (sortAttr.length > 1) {
                bDesc = !sortAttr[1].equalsIgnoreCase("asc");
            }

            String attr = query.sqlManager.getNc().getColName(sortAttr[0]);
            if (bDesc)
                query.desc(attr);
            else
                query.asc(attr);
        }
        return query;
    }

    /**
     * Jpa的Query在比较的时候传递的是对象，为了处理时间比较，做以下特殊处理
     *
     * @param value
     * @return
     */
    private static Comparable formatCompareValue(Field field, String value) {
        if (value == null || value.equalsIgnoreCase("null"))
            return null;
        String typeName = field.getGenericType().getTypeName();
        if (typeName.equals(Integer.class.getTypeName())) {
            return Integer.valueOf(value);
        } else if (typeName.equals(Double.class.getTypeName())) {
            return Double.valueOf(value);
        } else if (typeName.equals(Long.class.getTypeName())) {
            return Long.valueOf(value);
        } else if (typeName.equals(LocalDate.class.getTypeName())) {
            DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(value, dft);
        } else if (typeName.equals(LocalTime.class.getTypeName())) {
            DateTimeFormatter dft = DateTimeFormatter.ofPattern("HH:mm:ss");
            return LocalTime.parse(value, dft);
        } else if (typeName.equals(LocalDateTime.class.getTypeName())) {
            DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(value, dft);
        } else if (typeName.equals(Timestamp.class.getTypeName())) {
            return TimeUtils.parseDate(value);
        } else if (typeName.equals(java.sql.Date.class.getTypeName())) {
            return TimeUtils.parseDate(value);
        }
        return value;
    }

    private static Field getClassField(Class<?> tClass, String fieldName) {
        Field[] fields = tClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName))
                return field;
        }
        return null;
    }

    public static <T> Query getQueryByString(Query rootQuery, Class<T> tClass, String searchString, String sortString) {
        Query query = rootQuery;
        if (StringUtils.isNotBlank(searchString)) {
            RSQLVisitor<Query, Query> visitor = new BeetlQueryVisitor<T>(rootQuery, tClass);
            String rSql = searchString.replace(" like ", "==")
                    .replace(" notLike ", "!=")
                    .replace(" notIn ", "=out=")
                    .replace(" = ", "==")
                    .replace(" in ", "=in=");
            Node rootNode = new RSQLParser().parse(rSql);
            query = rootNode.accept(visitor);
            if (query == null)
                query = rootQuery;
        }
        if (StringUtils.isNotBlank(sortString)) {
            query = getSortByString(query, sortString);
        }
        return query;
    }

    public static void main(String[] args) {


    }


    private static class BeetlQueryVisitor<T> implements RSQLVisitor<Query, Query> {

        private final Class<T> clazz;
        private final Query rootQuery;

        public BeetlQueryVisitor(Query root, Class<T> clazz) {
            this.clazz = clazz;
            this.rootQuery = root;
        }

        public Query visit(LogicalNode node, Query query) {
            // logger.info("Creating Predicate for logical node: {}", node);
            if (query == null)
                query = this.rootQuery;

            // logger.info("Creating Predicates from all children nodes.");
            for (Node childNode : node.getChildren()) {
                Query newQuery = null;
                Query subQuery = query.condition();
                if (childNode instanceof LogicalNode)
                    newQuery = visit((LogicalNode) childNode, subQuery);

                if (childNode instanceof ComparisonNode)
                    newQuery = visit((ComparisonNode) childNode, subQuery);

                switch (node.getOperator()) {
                    case AND:
                        query.and(newQuery);
                        break;
                    case OR:
                        query.or(newQuery);
                        break;
                }
            }
            return query;
        }

        @Override
        public Query visit(AndNode node, Query builder) {
            return visit((LogicalNode) node, builder);
        }

        @Override
        public Query visit(OrNode node, Query builder) {
            return visit((LogicalNode) node, builder);
        }

        @Override
        public Query visit(ComparisonNode node, Query query) {
            // logger.info("{} {} {}", node.getSelector(), node.getOperator(), node.getArguments().get(0));
            ComparisonOperator operator = node.getOperator();
            Field field = getClassField(clazz, node.getSelector());
            if (field == null)
                return null;
            String valueStr = node.getArguments().get(0);
            String selector = node.getSelector();
            if (query == null)
                query = this.rootQuery;
            selector = query.sqlManager.getNc().getColName(selector);
            Comparable value = formatCompareValue(field, valueStr);
            switch (operator.getSymbol()) {
                case "==":
                    if (value instanceof String && valueStr.contains("%"))
                        query = query.andLike(selector, valueStr);
                    else
                        query = query.andEq(selector, value);
                    break;
                case "!=":
                    if (value instanceof String && valueStr.contains("%"))
                        query = query.andNotLike(selector, valueStr);
                    else
                        query = query.andNotEq(selector, value);
                    break;
                case "=in=":
                    query = query.andIn(selector, node.getArguments());
                    break;
                case "=out=":
                    query = query.andNotIn(selector, node.getArguments());
                    break;
                case "<=":
                case "=le=":
                    query = query.andLessEq(selector, value);
                    break;
                case ">=":
                case "=ge=":
                    query = query.andGreatEq(selector, value);
                    break;
                case ">":
                case "=gt=":
                    query = query.andGreat(selector, value);
                    break;
                case "<":
                case "=lt=":
                    query = query.andLess(selector, value);
                    break;
            }
            return query;
        }

    }
}