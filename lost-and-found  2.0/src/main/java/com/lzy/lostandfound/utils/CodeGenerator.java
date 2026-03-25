package com.lzy.lostandfound.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.query.SQLQuery;

import java.sql.Types;
import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        // MYSQL 示例 切换至SQL查询方式,需要指定好 dbQuery 与 typeConvert 来生成
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/lose_and_found", "root", "Cptbtptp8.")
                .dataSourceConfig(builder ->
                        builder.databaseQueryClass(SQLQuery.class)
                                .typeConvert(new MySqlTypeConvert())
                                .dbQuery(new MySqlQuery())

                ) .packageConfig(builder ->
                        builder.parent("com.lzy") // 设置父包名
                                .moduleName("lostandfound") // 设置父包模块名
                                .pathInfo(Collections.singletonMap(OutputFile.xml, "src/main/resources/mapper")) // 设置mapperXml生成路径
                )  .strategyConfig(builder ->
                        builder.addInclude("activities")
                                // 设置需要生成的表名

                )

                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
        // Other Config ...
    }
}
