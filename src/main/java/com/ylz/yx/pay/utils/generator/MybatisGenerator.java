package com.ylz.yx.pay.utils.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MybatisGenerator {
	 public static void main(String[] args) throws URISyntaxException {
	        try {
	            List<String> warnings = new ArrayList<String>();
	            boolean overwrite = true;
	            //直接获取generatorConfig.xml的文件路径 根据具体情况查看
				System.out.println(System.getProperty("user.dir"));
	            File configFile = new File("src/main/java/com/ylz/yx/pay/utils/generator/mybatisGeneratorConfig.xml");
	            ConfigurationParser cp = new ConfigurationParser(warnings);
	            Configuration config = cp.parseConfiguration(configFile);
	            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
	            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
	            myBatisGenerator.generate(null);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        } catch (InvalidConfigurationException e) {
	            e.printStackTrace();
	        } catch (XMLParserException e) {
	            e.printStackTrace();
	        }
	    }
	

}

