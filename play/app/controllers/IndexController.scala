package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._

import com.atilika.kuromoji.ipadic.Token
import com.atilika.kuromoji.ipadic.Tokenizer

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MutableMap}
import java.nio.file.{Path, Paths, Files}

case class WordCloud(text: String, value: String)

@Singleton
class IndexController @Inject() extends Controller {
  
  implicit val wcWrites = new Writes[WordCloud] {
    def writes(wc: WordCloud) = Json.obj(
      "text" -> wc.text,
      "value" -> wc.value
    )
  }
  
  /**
   * GET処理(HTML返却)
   */
  def get = Action {
    Ok(views.html.main())
  }
  
  /**
   * POST処理(JSON返却)
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
    // 解析結果登録マップ
    val map = MutableMap[String, Int]()
    // 解析
    val analysis = kuromoji(new Tokenizer)_
    // 登録
    val entry = entryMap(map)_
    /*
     * 解析実行
     * 歌詞ディレクトリからファイル名一覧を取得し、
     * 歌詞読み込み -> 解析 -> マップ登録
     * の順で実行する。
     */
    Files.newDirectoryStream(Paths.get("resources/text/bumpofchicken"), "*.txt").toList
      .map(readLyric).map(analysis).foreach(w => w.foreach(entry))
    
    /*
     * JSON変換し返却
     * マップを昇順ソートし、先頭から500件までに絞り込む。
     */
    Json.toJson(
        map.toSeq.sortBy(_._2).reverse.slice(0, 500)
        .map(e => WordCloud(e._1, e._2.toString))
      )
  }
  
  /**
   * 歌詞読み込み
   * 歌詞ファイルを読み込み、文字列として返却する。
   */
  def readLyric(path: Path):String = {
    Files.readAllLines(path).toList.reduceLeft(_+_)
  }
  
  /**
   * 形態素解析
   * 歌詞を解析し単語を基本形で抽出、
   * 名詞、動詞、形容詞以外の品詞は除く。
   */
  def kuromoji(tokenizer: Tokenizer)(lyric: String):List[String] = {
    tokenizer.tokenize(lyric).toList
      .filter(t => List("名詞", "動詞", "形容詞").contains(t.getPartOfSpeechLevel1))
      .map(t => if(t.getBaseForm == "*") t.getSurface else t.getBaseForm)
      .distinct
  }
  
  /**
   * マップ登録
   * ミュータブルマップに単語を登録する。
   * すでに登録されている単語の場合、カウントを１増やし更新する。
   */
  def entryMap(cloudMap: MutableMap[String, Int])(text: String) = {
    cloudMap(text) = cloudMap.getOrElse(text, 0) + 1
  }
  
}
