package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._

import com.atilika.kuromoji.ipadic.Token
import com.atilika.kuromoji.ipadic.Tokenizer

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MutableMap}
import java.nio.file.{Path, Paths, Files}

case class WordCloud(val text: String, value: String)

@Singleton
class IndexController @Inject() extends Controller {
  
  implicit val wcWrites = new Writes[WordCloud] {
    def writes(wc: WordCloud) = Json.obj(
      "text" -> wc.text,
      "value" -> wc.value
    )
  }
  
  val tokenizer = new Tokenizer
  
  /**
   * GET処理
   */
  def get = Action {
    Ok(views.html.main())
  }
  
  /**
   * POST処理
   */
  def post = Action(parse.json) { request =>
    (request.body \ "event").asOpt[String].map { event =>
      event match {
        case "cloud" => Ok(cloud)
        case _ => BadRequest("No Event") 
      }
    }.getOrElse {
      BadRequest("No Event")
    }
  }
  
  /**
   * WordCloud処理
   */
  def cloud: JsValue = {
    val map = MutableMap[String, Int]()
    val addText = addTextToMap(map)
    val dir = Paths.get("resources/text/bumpofchicken")
    Files.newDirectoryStream(dir, "*.txt").toList
      .map(readLyric)
      .map(kuromoji)
      .foreach(w => w.foreach(addText))
    
    val result = map.toSeq.filter(_._2 >= 3).sortBy(_._2).map(e => WordCloud(e._1, e._2.toString))
    
    println(map.size)
    println(result.size)
        
    Json.toJson(result)
  }
  
  /**
   * 歌詞読み込み
   * 歌詞ファイルを読み込み、文字列として返却する。
   */
  def readLyric(path: Path):String = {
    val sb = new StringBuilder
    Files.readAllLines(path).toList.foreach(sb.append)
    sb.toString
  }
  
  /**
   * 形態素解析
   * 入力された歌詞を名詞、動詞、形容詞に分解し、他の品詞は除く。
   * 動詞は基本形に変換しています。
   */
  def kuromoji(lyric: String):List[String] = {
    tokenizer.tokenize(lyric).toList
      .filter(t => List("名詞", "動詞", "形容詞").contains(t.getPartOfSpeechLevel1))
      .map(t => if(t.getBaseForm == "*") t.getSurface else t.getBaseForm)
      .distinct
  }
  
  /**
   * マップ追加
   * ミュータブルマップにテキストを追加する。
   * すでに登録されているテキストの場合、カウントを増加する。
   */
  def addTextToMap = (cloudMap: MutableMap[String, Int]) => (text: String) => {
    if (cloudMap.contains(text)) {
      cloudMap(text) = cloudMap.get(text).get + 1
    } else { 
      cloudMap(text) = 1
    }
  }
  
}
