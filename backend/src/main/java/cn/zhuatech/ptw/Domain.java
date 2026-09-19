/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.ptw;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static cn.zhuatech.ptw.Model.*;
import static cn.zhuatech.ptw.Engine.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static Map<String,Object> copy(Row r){return new LinkedHashMap<>(r.data());}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal n(Row r,String k){return num(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal z(Map<String,Object>d,String k){return d.containsKey(k)?num(d,k):BigDecimal.ZERO;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String t(Row r,String k){return txt(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static List<Row> linked(Engine e,User u,String module,String key,String id){return e.all(u,module).stream().filter(r->t(r,key).equals(id)).toList();}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void unique(Engine e,User u,String module,Map<String,Object>d,String key){require(e.all(u,module).stream().noneMatch(r->t(r,key).equalsIgnoreCase(txt(d,key))),"重复的"+key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void dates(Map<String,Object>d,String from,String to){require(!date(d,to).isBefore(date(d,from)),"结束日期不能早于开始日期");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void change(Engine e,User u,Row row,String state,Map<String,Object>d,String note){e.save(u,row,state,d,"LINKED",note);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("readings")){require(e.ref(u,r.data(),"job","jobs").state().equals("RUNNING")&&txt(d,"job").equals(t(r,"job")),"仅进行中的任务可以修改测量值，且不得迁移任务");require(linked(e,u,"readings","job",t(r,"job")).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"point").equals(txt(d,"point"))),"测量点编号重复");return;}
  if(r.module().equals("versions")){require(e.ref(u,r.data(),"artwork","artworks").state().equals("DRAFT"),"已送审稿件不可修改");require(txt(d,"artwork").equals(t(r,"artwork"))&&num(d,"revision").compareTo(n(r,"revision"))==0,"版本不能迁移任务或改写版本号");return;}
  for(var m:e.spec().modules())for(Row other:e.all(u,m.key()))if(!other.id().equals(r.id())&&other.data().values().stream().anyMatch(v->r.id().equals(v)))throw new Failure(409,"资料已有下游引用，请新建版本而不是改写历史");
  var fields=e.spec().module(r.module()).fields().stream().map(Field::key).toList();
  r.data().forEach((k,v)->{if(!fields.contains(k))d.put(k,v);});
  if(d.containsKey("start")&&d.containsKey("end"))dates(d,"start","end");
  if(d.containsKey("from")&&d.containsKey("to"))dates(d,"from","to");
  for(String key:List.of("serial","sku","invoice","invoiceNo","lockNo"))if(d.containsKey(key))require(e.all(u,r.module()).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,key).equalsIgnoreCase(txt(d,key))),"重复唯一业务标识: "+key);
  if(d.containsKey("bonusRate"))require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");
  if(d.containsKey("lifeLimit"))require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");
  if(d.containsKey("defects"))require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能超过生产次数");
  if(d.containsKey("nps"))require(num(d,"nps").compareTo(BigDecimal.TEN)<=0&&num(d,"csat").compareTo(new BigDecimal("5"))<=0,"评价分数超出范围");
  if(d.containsKey("oxygenMin"))require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
  if(r.module().equals("invoices"))require(e.all(u,"invoices").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"shipment").equals(txt(d,"shipment"))),"运单已关联结算账单");
  if(r.module().equals("sales")){Row program=e.ref(u,d,"program","programs");require(program.state().equals("ACTIVE")&&!date(d,"soldAt").isBefore(date(program.data(),"start"))&&!date(d,"soldAt").isAfter(date(program.data(),"end")),"协议状态或销售日期无效");}
  if(r.module().equals("jobs")){Row instrument=e.ref(u,d,"instrument","instruments"),standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED")&&t(instrument,"unit").equals(t(standard,"unit")),"器具状态或计量单位无效");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
  if(r.module().equals("permits")){require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天");require(t(e.ref(u,d,"isolation","isolations"),"location").equals(txt(d,"location")),"隔离区域不匹配");}
  if(r.module().equals("responses")){require(e.all(u,"responses").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"survey").equals(txt(d,"survey"))&&t(x,"customer").equals(txt(d,"customer"))),"客户已存在该问卷反馈");require(t(e.ref(u,d,"customer","customers"),"consent").equals("YES"),"客户未允许反馈邀请");}
  if(r.module().equals("products")){String barcode=txt(d,"barcode");require(barcode.matches("\\d{13}"),"条码须为 EAN-13");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}

 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){
  var out=new LinkedHashMap<String,Object>();out.put("现场作业",e.all(u,"permits").stream().filter(r->r.state().equals("ACTIVE")).count());out.put("暂停作业",e.all(u,"permits").stream().filter(r->r.state().equals("SUSPENDED")).count());out.put("有效隔离",e.all(u,"isolations").stream().filter(r->r.state().equals("LOCKED")).count());;return out;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){switch(module){case "locations" -> require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
case "isolations" -> unique(e,u,module,d,"lockNo");
case "permits" -> {dates(d,"start","end");require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天，超期应重新申请");Row iso=e.ref(u,d,"isolation","isolations");require(t(iso,"location").equals(txt(d,"location")),"隔离区域与作业区域不符");}
case "hazards" -> require(e.ref(u,d,"permit","permits").state().equals("DRAFT"),"只允许在草稿申请中添加风险项");
case "readings" -> {Row permit=e.ref(u,d,"permit","permits");require(!permit.state().equals("CLOSED"),"已完工作业不允许补录检测");require(!date(d,"testedAt").isAfter(LocalDate.now()),"检测日期不能为未来");} default -> {} }}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  String k=r.module()+"."+action;switch(k){
case "isolations.lock" -> {require(e.all(u,"isolations").stream().noneMatch(x->!x.id().equals(r.id())&&x.state().equals("LOCKED")&&t(x,"location").equals(txt(d,"location"))&&t(x,"equipment").equals(txt(d,"equipment"))),"相同设备已有隔离");d.putAll(i);}
case "isolations.release" -> {require(e.all(u,"permits").stream().noneMatch(p->t(p,"isolation").equals(r.id())&&Set.of("APPROVED","ACTIVE","SUSPENDED").contains(p.state())),"仍有有效作业许可证占用隔离，不能解除");d.putAll(i);}
case "hazards.verify" -> {require(Set.of("DRAFT","REVIEW").contains(e.ref(u,d,"permit","permits").state()),"作业已放行，禁止改写风险验证");d.putAll(i);d.put("verifiedBy",u.username());}
case "permits.submit" -> require(!linked(e,u,"hazards","permit",r.id()).isEmpty(),"至少识别一项危险因素");
case "permits.approve","permits.start","permits.resume" -> {
 Row iso=e.ref(u,d,"isolation","isolations");require(iso.state().equals("LOCKED"),"隔离未挂牌或已解除");require(t(iso,"location").equals(txt(d,"location")),"隔离区域不匹配");LocalDate today=LocalDate.now();require(!today.isBefore(date(d,"start"))&&!today.isAfter(date(d,"end")),"许可证不在有效期内");
 var hazards=linked(e,u,"hazards","permit",r.id());require(!hazards.isEmpty()&&hazards.stream().allMatch(x->x.state().equals("VERIFIED")),"所有风险控制措施都必须验证");
 if(!txt(d,"type").equals("MAINTENANCE")){
  var gas=linked(e,u,"readings","permit",r.id());require(!gas.isEmpty(),"缺少气体检测");Row latest=gas.getLast();require(date(latest.data(),"testedAt").equals(today),"气体检测不是当日记录");
  Row location=e.ref(u,d,"location","locations");require(n(latest,"oxygen").compareTo(n(location,"oxygenMin"))>=0&&n(latest,"oxygen").compareTo(n(location,"oxygenMax"))<=0&&n(latest,"lel").compareTo(n(location,"lelMax"))<0&&n(latest,"co").compareTo(n(location,"coMax"))<=0&&n(latest,"h2s").compareTo(n(location,"h2sMax"))<=0,"气体检测超出区域配置阈值，禁止放行");
  d.put("gasReading",latest.id());
 }
 d.put("lastGateBy",u.username());d.put("lastGateDate",today.toString());
}
case "permits.suspend","permits.close" -> d.putAll(i);
case "permits.cancel" -> d.putAll(i);
 default -> {} }return null;
 }
}
