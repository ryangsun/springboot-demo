package com.example.demo;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.repository.SchemaRepository;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSONArray;
import com.bumao.model.table2entry.ToEntry;
import com.bumao.model.table2entry.domain.TableEntryVo;
import com.example.demo.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@Slf4j
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		System.out.println("Bumao started.");
	}

	@GetMapping("/h")
	public String h() {
		String sql = "CREATE TABLE `card` (\n" +
				"  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'PK',\n" +
				"  `batch_id` int(11) NOT NULL COMMENT '所属批次ID',\n" +
				"  `denomination` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '面额',\n" +
				"  `card_no` varchar(20) NOT NULL COMMENT '实体卡号',\n" +
				"  `card_pass` varchar(20) NOT NULL COMMENT '实体密码',\n" +
				"  `status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '状态，0=未核销 1=已核销',\n" +
				"  `active_time` datetime DEFAULT NULL COMMENT '核销核销时间',\n" +
				"  `active_userid` varchar(32) DEFAULT NULL COMMENT '核销用户ID',\n" +
				"  `active_mobile` varchar(20) DEFAULT NULL COMMENT '核销时用户的手机号码',\n" +
				"  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
				"  `stop_time` datetime NOT NULL COMMENT '结束核销时间',\n" +
				"  `active_openid` varchar(64) DEFAULT NULL COMMENT '用户openid',\n" +
				"  PRIMARY KEY (`id`) USING BTREE,\n" +
				"  KEY `batch_id` (`batch_id`) USING BTREE,\n" +
				"  KEY `status` (`status`) USING BTREE\n" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡券批次表';" +
				"select * from card;";
		ToEntry toEntry = new ToEntry();
		List<TableEntryVo> tableEntryVos = toEntry.getEntry(sql);
		log.info("tableEntryVos={}", JSONArray.toJSON(tableEntryVos));

		return "a";
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
//		sql2();
		String sql = "CREATE TABLE `card` (\n" +
				"  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'PK',\n" +
				"  `batch_id` int(11) NOT NULL COMMENT '所属批次ID',\n" +
				"  `denomination` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '面额',\n" +
				"  `card_no` varchar(20) NOT NULL COMMENT '实体卡号',\n" +
				"  `card_pass` varchar(20) NOT NULL COMMENT '实体密码',\n" +
				"  `status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '状态，0=未核销 1=已核销',\n" +
				"  `active_time` datetime DEFAULT NULL COMMENT '核销核销时间',\n" +
				"  `active_userid` varchar(32) DEFAULT NULL COMMENT '核销用户ID',\n" +
				"  `active_mobile` varchar(20) DEFAULT NULL COMMENT '核销时用户的手机号码',\n" +
				"  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
				"  `stop_time` datetime NOT NULL COMMENT '结束核销时间',\n" +
				"  `active_openid` varchar(64) DEFAULT NULL COMMENT '用户openid',\n" +
				"  PRIMARY KEY (`id`) USING BTREE,\n" +
				"  KEY `batch_id` (`batch_id`) USING BTREE,\n" +
				"  KEY `status` (`status`) USING BTREE\n" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡券批次表';" +
				"select * from card;";
		String dbType = JdbcConstants.MYSQL;

		//格式化输出
//		String result = SQLUtils.format(sql, dbType);
//		System.out.println(result); // 缺省大写格式
		//转list
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);

		//解析出的独立语句的个数
		log.info("stmtList.size={}",stmtList.size());
		for (int i = 0; i < stmtList.size(); i++) {
			SQLStatement stmt = stmtList.get(i);
			if(stmt instanceof MySqlCreateTableStatement){
				log.info("{} is MySqlCreateTableStatement-------",i);
				this.parseCreateTable((MySqlCreateTableStatement)stmt);
			}else if(stmt instanceof SQLSelectStatement){
				log.info("{} is SQLSelectStatement",i);
			}else{
				log.info("{} 未知的stmt类型",i);
				log.info("stmt.getClass()={}",stmt.getClass());
			}
			MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
//			stmt.accept(visitor);
//			//获取表名称
////			System.out.println("Tables : " + visitor.getCurrentTable());
//			//获取操作方法名称,依赖于表名称
			System.out.println("Manipulation : " + visitor.getTables());
//			//获取字段名称
//			System.out.println("fields : " + visitor.getColumns());
		}

		return String.format("Hello %s!", name);
	}
	private void sql2(){
		final String dbType =JdbcConstants.MYSQL;
		SchemaRepository repository = new SchemaRepository(dbType);

		repository.console("use sc00;");
		String sql = "CREATE TABLE `card` (\n" +
				"  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'PK',\n" +
				"  `batch_id` int(11) NOT NULL COMMENT '所属批次ID',\n" +
				"  `denomination` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '面额',\n" +
				"  `card_no` varchar(20) NOT NULL COMMENT '实体卡号',\n" +
				"  `card_pass` varchar(20) NOT NULL COMMENT '实体密码',\n" +
				"  `status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '状态，0=未核销 1=已核销',\n" +
				"  `active_time` datetime DEFAULT NULL COMMENT '核销核销时间',\n" +
				"  `active_userid` varchar(32) DEFAULT NULL COMMENT '核销用户ID',\n" +
				"  `active_mobile` varchar(20) DEFAULT NULL COMMENT '核销时用户的手机号码',\n" +
				"  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
				"  `stop_time` datetime NOT NULL COMMENT '结束核销时间',\n" +
				"  `active_openid` varchar(64) DEFAULT NULL COMMENT '用户openid',\n" +
				"  PRIMARY KEY (`id`) USING BTREE,\n" +
				"  KEY `batch_id` (`batch_id`) USING BTREE,\n" +
				"  KEY `status` (`status`) USING BTREE\n" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡券批次表';";
		String a = repository.console(sql);
		System.out.println("a="+a);
		MySqlCreateTableStatement createTableStmt =(MySqlCreateTableStatement) repository.findTable("card").getStatement();
		this.parseCreateTable(createTableStmt);

//		Map<String,Object> mapp = createTableStmt.getAttributes();
//		for (Map.Entry entry : mapp.entrySet()) {
//			System.out.println(entry.getKey() + "=" + entry.getValue());
//		}
//		System.out.println("--------------------");
//		MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
//		createTableStmt.accept(visitor);
//
//		Collection<TableStat.Column> c = visitor.getColumns();
//		for (TableStat.Column cc:c){
//			System.out.println( "--"+cc.getName() +" : "+ cc.getDataType());
//			Map<String,Object> cca = cc.getAttributes();
//			System.out.println("getAttributes.size="+cca.size());
//			for (Map.Entry<String, Object> entry : cca.entrySet()) {
//				try{
//					System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//				}catch (Exception e) {
//					System.out.println("Key = " + entry.getKey());
//				}
//			}
//		}
//
//		System.out.println("--------------------");
//		List<SQLColumnDefinition> sqlColumnDefinitionList = createTableStmt.getPartitionColumns();
//		System.out.println("sqlColumnDefinitionList.size="+sqlColumnDefinitionList.size());
//		for(SQLColumnDefinition sqlColumnDefinition:sqlColumnDefinitionList){
//			System.out.println(sqlColumnDefinition.getColumnName() );
//			System.out.println(sqlColumnDefinition.getComment() );
//			System.out.println(sqlColumnDefinition.getDataType() );
//		}
//		System.out.println("--------------------");
//
//		a = repository.console("show columns from card");
//		System.out.println("a="+a);
	}
	private void parseCreateTable(MySqlCreateTableStatement createTableStmt){
		//table属性
		log.info("tablename={} , commit={}",createTableStmt.getName(),createTableStmt.getComment());
		Map<String, SQLObject> tmap = createTableStmt.getTableOptions();
		if(tmap.get("ENGINE")!=null){
			log.info("table ENGINE={}",tmap.get("ENGINE"));
		}
		if(tmap.get("CHARSET")!=null){
			log.info("table CHARSET={}",tmap.get("CHARSET"));
		}
		//内容属性
		List<SQLTableElement> sqlTableElementList = createTableStmt.getTableElementList();
		log.info("sqlTableElementList size={}",sqlTableElementList.size());
		int i=0;
		for(SQLTableElement sqlTableElement:sqlTableElementList){
			i++;
//			log.info("{} - sqlTableElement.getClass()={}",i,sqlTableElement.getClass() );
			if(sqlTableElement instanceof SQLColumnDefinition) {
				SQLColumnDefinition sqlColumnDefinition = (SQLColumnDefinition) sqlTableElement;
				log.info("{}- 字段={},类型={},默认值={},备注={}"
						,i
						,sqlColumnDefinition.getName()
						,sqlColumnDefinition.getDataType()
						,sqlColumnDefinition.getDefaultExpr()
						,sqlColumnDefinition.getComment()
//						,sqlColumnDefinition.getCollateExpr()
//						,sqlColumnDefinition.getCharsetExpr()
				);
			}else if(sqlTableElement instanceof MySqlPrimaryKey){
				MySqlPrimaryKey mySqlPrimaryKey  = (MySqlPrimaryKey) sqlTableElement;
				log.info("{}- 主键={},类型={},字段={},备注={}"
						,i
						,mySqlPrimaryKey.getName()
						,mySqlPrimaryKey.getIndexType()
						,this.parseColumns(mySqlPrimaryKey.getColumns())
						,mySqlPrimaryKey.getComment()
				);
			}else if(sqlTableElement instanceof MySqlKey){
				MySqlKey mySqlKey = (MySqlKey) sqlTableElement;
				log.info("{}- 索引={},类型={},字段={},备注={}"
						,i
						,mySqlKey.getName()
						,mySqlKey.getIndexType()
						,this.parseColumns(mySqlKey.getColumns())
						,mySqlKey.getComment()
				);
//				SQLASTVisitor v = new SQLASTOutputVisitor();
//				mySqlKey.accept(SQLASTVisitor v);
			}
		}
	}
	private List<SQLExpr> parseColumns(List<SQLSelectOrderByItem>  sqlSelectOrderByItems){
		List<SQLExpr> retList = new ArrayList<>();
		for(SQLSelectOrderByItem  items :sqlSelectOrderByItems){
			SQLExpr expr = items.getExpr();
			retList.add(expr);
		}
		return retList;
	}

	@Autowired
	private RedisUtils redisUtils;
	@RequestMapping(value = "/hello/{id}")
	public String helloRedis(@PathVariable(value = "id") String id){
		//查询缓存中是否存在
		boolean hasKey = redisUtils.exists(id);
		String str = "abc";
		if(hasKey){
			//获取缓存
			Object object =  redisUtils.get(id);
			log.info("从缓存获取的数据"+ object);
			str = object.toString();
		}else{
			//从数据库中获取信息
			log.info("从数据库中获取数据");
//			str = testService.test();
			//数据插入缓存（set中的参数含义：key值，user对象，缓存存在时间10（long类型），时间单位）
			redisUtils.set(id,str,10L, TimeUnit.MINUTES);
			log.info("数据插入缓存" + str);
		}
		return str;
	}
}

