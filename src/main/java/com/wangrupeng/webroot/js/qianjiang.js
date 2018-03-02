/**
 * Created by wangr on 2017/11/17.
 */
$(function () {
    setTimeout(function () {
        //console.log('ok');
        //连接websocket后端服务器

        var eb = new EventBus("http://192.168.1.6:3000/eventbus");
        //监听服务器开始解析url流
        //监听redis订阅的消息
        eb.onopen = function () {
            eb.registerHandler("news-feed", function (err, msg) {

                //数据：
                var message = msg.body;
                // console.log(JSON.parse(message));
                var data = JSON.parse(message);
                if (data) {
                    SearchNum++;
                    $("#searchRes").html("查询结果：<span>" + SearchNum + "</span>对");
                    $("#reviewThan").html("复核比对：<span>" + SearchNum + "</span>对");
                }
                //console.log(data);
                var str = "";
                var faceurl = get_hdfsurl(data[8]);
                var blackUrl = get_hdfsurl(data[6]);//identifyImg
                var frameUrl = get_hdfsurl(data[7])
                var gender = data[2] ? "女" : "男";
                var sim = data[9] + "";
                sim = sim.substring(0, 5);
                sim = Math.round(sim * 1000) / 10;
                //console.log(sim);
                str = '<ul data-id="' + data[0] + '"><li><img class="faceImg" src="' + faceurl + '" alt=""/></li>'
                    + '<li><img class="blackImg" src="' + blackUrl + '" alt=""/></li>'
                    + '<li><p class="blackName">姓名: ' + data[1] + '</p>'
                    + '<p class="blackAge">年龄: ' + data[3] + '岁</p>'
                    + '<p class="blackSex">性别: ' + gender + '</p>'
                    + '<p class="blackSim">相似度: ' + sim + '%</p></li>'
                    + '<li class="blackTime" title="' + data[5] + '">' + fullTime(data[5]) + '</li>'
                    + '<li class="blackCam">' + data[4] + '号摄像头</li>'
                    + '<li  style="display: none"><img class="frameUrl" src="' + frameUrl + '" alt=""/></li>'
                    + '</ul>';
                $(".con-box").prepend(str);
                //固定推流数
                var last = document.getElementsByClassName("con-box")[0].lastElementChild;
                //console.log(first);
                if ($(".con-box>ul").length > 150) {
                    document.getElementsByClassName("con-box")[0].removeChild(last);
                }

                //点击每一个列让对应的详细信息展示出来
                $(".con-box>ul").on("click", function (e) {
                    var datas = {
                        personId: $(this).attr("data-id"),
                        timestamp: $(this).find(".blackTime").attr("title")
                    }
                    $(this).css("backgroundColor", "rgba(255,255,255,.2)").siblings().css("backgroundColor", "rgba(255,255,255,0)");
                    var thisInfo = {
                        "id": $(this).attr("data-id"),
                        "faceImg": $(this).find(".faceImg").attr("src"),
                        "blackImg": $(this).find(".blackImg").attr("src"),
                        "blackName": $(this).find(".blackName").html(),
                        "blackAge": $(this).find(".blackAge").html(),
                        "blackSex": $(this).find(".blackSex").html(),
                        "blackSim": $(this).find(".blackSim").html(),
                        "blackTime": $(this).find(".blackTime").html(),
                        "blackCam": $(this).find(".blackCam").html(),
                        "frameUrl": $(this).find(".frameUrl").attr("src"),
                        "timeStemp": $(this).find(".blackTime").attr("title")
                    }
                    $.ajax({
                        url: "/select.json",
                        data: datas,
                        success: function (data) {
                            //console.log(data);
                            thisInfo.judgeResult = data.person.judgeResult;
                            thisInfo.judgeWay = data.person.judgeWay;
                            //console.log(thisInfo);
                            thisInfo = JSON.stringify(thisInfo);
                            sessionStorage.setItem("clickBlackInfo", thisInfo)
                            $('#infoShowBox').attr('src', $('#infoShowBox').attr('src'));
                            setTimeout(function () {
                                $("#infoShowBox").css("display", "block");
                            }, 100)
                        }
                    })
                })
            })
        };
        function parseURL(url) {
            var a = document.createElement('a');
            a.href = url;
            return {
                source: url,
                protocol: a.protocol.replace(':', ''),
                host: a.hostname,
                port: a.port,
                query: a.search,
                params: (function () {
                    var ret = {},
                        seg = a.search.replace(/^\?/, '').split('&'),
                        len = seg.length, i = 0, s;
                    for (; i < len; i++) {
                        if (!seg[i]) {
                            continue;
                        }
                        s = seg[i].split('=');
                        ret[s[0]] = s[1];
                    }
                    return ret;
                })(),
                file: (a.pathname.match(/\/([^\/?#]+)$/i) || [, ''])[1],
                hash: a.hash.replace('#', ''),
                path: a.pathname.replace(/^([^\/])/, '/$1'),
                relative: (a.href.match(/tps?:\/\/[^\/]+(.+)/) || [, ''])[1],
                segments: a.pathname.replace(/^\//, '').split('/')
            };
        }

        function get_hdfsurl(url) {
            var tmp = "http" + url.substring(4);
            // console.log(url)
            //console.log("解析");
            var myurl = parseURL(tmp);
            var hdfs_url = "http://192.168.1.6:50070/webhdfs/v1" + myurl.path + "?user.name=hadoop&op=open";
            return hdfs_url;
        }
    }, 200)
    function fullTime(time1) {
        var time = new Date(time1 - 0);
        var getYear = time.getFullYear();
        var getMounth = time.getMonth() + 1;
        getMounth = getMounth >= 10 ? getMounth : "0" + getMounth;
        var getDay = time.getDate();
        getDay = getDay >= 10 ? getDay : "0" + getDay;
        var getHour = time.getHours();
        getHour = getHour >= 10 ? getHour : "0" + getHour;
        var getMinutes = time.getMinutes();
        getMinutes = getMinutes >= 10 ? getMinutes : "0" + getMinutes;
        var getSecond = time.getSeconds();
        getSecond = getSecond >= 10 ? getSecond : "0" + getSecond;
        // console.log(getYear,getMounth,getDay,getHour,getMinutes,getSecond);
        var timeStr = getYear + "-" + getMounth + "-" + getDay + "<br/>  " + getHour + ":" + getMinutes + ":" + getSecond;
        return timeStr;
    }
})