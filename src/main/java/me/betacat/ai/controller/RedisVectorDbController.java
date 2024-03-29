package me.betacat.ai.controller;


import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RedisVectorDbController {

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter;

    @Autowired
    public RedisVectorDbController(VectorStore vectorStore, TokenTextSplitter tokenTextSplitter) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
    }


    private int topK = 4;
    private double similarityThreshold = 0.275;


    @GetMapping("/ai/init-vector")
    public String initVector() {
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("country", "UK", "year", 2020)),
                new Document("The World is Big and Salvation Lurks Around the Corner", Map.of()),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("country", "NL", "year", 2023)));

        vectorStore.add(documents);

        return "OK";
    }


    @GetMapping("/ai/init-vector2")
    public String initVector2() {
        List<Document> documents = List.of(
                new Document("新赛季NBA全面拉开帷幕，在湖人此前进行的两场季前赛中，一胜一负，和篮网一战，全主力打了半场，詹姆斯和拉塞尔两个持球点，带动全队8人得分上双，经过今夏的补强，湖人整个轮转框架深度明显提升，哈姆完全可以摆出两套竞争力阵容。第三场对决对手是国王，国王今年夏天5年2.17亿美元续约萨博尼斯，此外交易得到杜阿尔特，在上赛季常规赛西部第三的阵容基础上，继续升级，新赛季也有望成为太阳、勇士、湖人、掘金冲冠的搅局者。湖人今夏引援收获颇丰，普林斯的投射能力，海斯的机动性和篮下终结效率，伍德也在慢慢改掉重攻轻守的坏习惯。", Map.of()),
                new Document("本场比赛前，哈姆透露詹姆斯+里夫斯+浓眉三人组均休战，此外范德比尔特和雷迪什两人，因为脚踝伤势和跟腱伤势也无法出场，湖人几乎相当于让出一整套首发阵容。但哈姆赛前信心十足，表示如今球队可用球员很多，每个人都充满竞争力。湖人摆出的首发为：拉塞尔+文森特+普林斯+八村塁+海斯，算上替补席的伍德，以及缺阵的几人，这基本上是湖人新赛季的主要轮转框架。而国王这边除了穆雷因伤休战外，也拿出最强阵容对垒湖人：福克斯+许尔特+巴恩斯+韦津科夫+萨博尼斯。尴尬的是，国王几乎全主力，却打不过湖人的替补！浓眉在场边非常开心。", Map.of()),
                new Document("开局文森特空切篮下吃饼，巴恩斯和文森特对飙三分球，八村塁内外开花，拉塞尔飘逸跳投，湖人形成多点开花之势。湖人在防守端的压迫性十足，海斯一直在对抗萨博尼斯，而且相比于上赛季，湖人退防速度有明显提升，八村塁也敢打敢拼，顶着萨博尼斯强起造犯规。中段当国王迫近分差时，拉塞尔连续持球攻框得手，拉塞尔打得非常聪明，几次突分喂饼普林斯和伍德，审时度势挡拆后突破，国王确实拦不住。而节末湖人陷入得分荒，国王二阵打湖人三阵时，蒙克和莱尔斯的三分球带队缩小分差，第一节湖人30-28领先国王。", Map.of()),
                new Document("第二节开打，文森特连珠炮弹轰进三分，普林斯高难度强投打成2+1，萨博尼斯一直在点名海斯造杀伤，从力量对抗来说，海斯有些吃力。许尔特的三分+福克斯的上篮，国王中段终于抹平分差。但节末拉塞尔轻松上篮得手，助攻普林斯再中三分球，湖人进攻延续性很好。国王则凭借萨博尼斯和福克斯连续罚球上分，半场51-50领先湖人。看得出国王主帅迈克布朗确实想赢球，增加首发上场时间，基本按照常规赛节奏打，哈姆在锻炼第二阵容的同时，也给了小将克里斯蒂更多时间，确实湖人仅靠里弗斯+拉塞尔+文森特三后卫，延续一整年有些吃力。", Map.of()),
                new Document("下半场回来，文森特和巴恩斯对飙三分，国王在外线打开手感后，一直压制住湖人，但中段湖人突然爆发：拉塞尔造三分犯规+反击轻松上篮+突分助攻普林斯轰进三分球+控球过半场后连续两次突施冷箭三分打停国王！湖人打出一波15-3的高潮瞬间反超并拉开分差。福克斯的三分球帮助国王止血，伍德上空篮得手，克里斯蒂反击单臂扣杀，而到节末，克里斯蒂和伍德两人一举将分差拉开到两位数，三节打完湖人90-79领先国王。打到第四节，当湖人已经拿出第三阵容时，国王还是蒙克带队的第二阵容，可即便如此，国王还是无法缩小分差。", Map.of()),
                new Document("反而是湖人在卡尔斯顿+席菲诺的带领下，继续扩大领先优势，麦基面对年轻的克里斯蒂和席菲诺，速度跟不上，还屡屡吃到犯规。最终湖人109-101完胜国王，迎来季前赛两连胜！数据，国王这边，萨博尼斯10分16篮板5助攻，福克斯18分5助攻，杜阿尔特10分2篮板，巴恩斯15分。湖人这边，拉塞尔21分3篮板8助攻，八村塁13分6篮板，普林斯13分7篮板2抢断，文森特18分2助攻，伍德13分4篮板。湖人替补逆袭国王首发，而湖人第三阵容又能压制国王替补，这让人始料未及。", Map.of()),
                new Document("纵观全场比赛，湖人今非昔比。首先能得分的点太多了，拉塞尔和湖人续约1+1合同，他确实是置死地而后生，他也需要更好的表现为自己下份大合同蓄力。普林斯+文森特包括八村塁，湖人如今也不缺外线投手，更有海斯和伍德这种机动性很强的内线参与护框，整个湖人几乎没有短板。只要哈姆不执着于摆上三个小个后卫，詹眉身边保留一个做脏活的蓝领锋线，保持健康状态，新赛季湖人的上限不可限量。", Map.of())
        );

        // 通过文本分割器进行处理
        vectorStore.add(tokenTextSplitter.apply(documents));

        return "OK";
    }



    @GetMapping("/ai/search-vector")
    public List<Document> searcheVector(@RequestParam(value = "message", defaultValue = "Spring") String message) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withTopK(5));
        return results;
    }

    @GetMapping("/ai/search-vector2")
    public List<Document> searcheVector2(@RequestParam(value = "message", defaultValue = "The World") String message) {

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest
                        .query(message)
                        .withTopK(topK)
                        .withSimilarityThreshold(similarityThreshold)
                        .withFilterExpression("country in ['UK', 'NL'] && year >= 2020"));
        return results;
    }


    @GetMapping("/ai/search-vector3")
    public List<Document> searcheVector3(@RequestParam(value = "message", defaultValue = "The World") String message) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        List<Document> results =  vectorStore.similaritySearch(
                SearchRequest
                        .query("The World")
                        .withTopK(topK)
                        .withSimilarityThreshold(similarityThreshold)
                        .withFilterExpression(b.and(
                                b.in("country", "UK", "NL"),
                                b.gte("year", 2021)).build()));
        return results;
    }

}
