package com.ylz.yx.pay.utils.generator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.DefaultCommentGenerator;

/**
 *@auth thj
 *@time 2018-05-11 15:23:42
 */
public class MybatisCommentGenerator extends DefaultCommentGenerator {

	private boolean suppressAllComments;

	/**
	 * 字段属性注释
	 */
	public void addFieldComment(Field field, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
		if (suppressAllComments) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		field.addJavaDocLine("/**");
		sb.append(" * ");
		sb.append(introspectedColumn.getRemarks());
		field.addJavaDocLine(sb.toString());
		field.addJavaDocLine(" */");
//		field.addJavaDocLine("@ApiModelProperty(\""+introspectedColumn.getRemarks()+"\")");
	}

	/**
	 * get方法注释
	 */
	public void addGetterComment(Method method, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
	}

	/**
	 * set注释
	 */
	public void addSetterComment(Method method, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
	}

	public void addModelClassComment(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
//
//		topLevelClass.addJavaDocLine("import io.swagger.annotations.ApiModelProperty;");
//		topLevelClass.addJavaDocLine("import io.swagger.annotations.ApiModel;");
//		topLevelClass.addJavaDocLine("@ApiModel");
	}


	// 添加xml文件的注释
	public void addComment(XmlElement xmlElement) {

	}
}