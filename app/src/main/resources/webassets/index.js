const queue = document.getElementById('queue');
const review = document.getElementById('review');

const question_queue = document.getElementById('question-queue');
const question_review = document.getElementById('question-review');

const prefix = document.getElementById('prefix');
const managerreview = document.getElementById('managerreview');
const reviewchannel = document.getElementById('reviewchannel');
const embedcolor = document.getElementById('embedcolor');

const permissionrole = document.getElementById('permissionrole');
const managerrole = document.getElementById('managerrole');
const updateconfig = document.getElementById('updateconfig');


const list_title_text = document.getElementById('list-title-text');

var qotdColor = "#000";

function deleteQOTD(uuid) {
    httpPostAsync(`/api/v1/delete`, `{"uuid":"${uuid}"}`, (res) => {
        window.location.reload();
    });
}

queue.addEventListener("click", function () {
    question_queue.style.display = "block";
    question_review.style.display = "none";

    list_title_text.innerHTML = "Queue:";

    getQueue();
});

review.addEventListener("click", function () {
    question_queue.style.display = "none";
    question_review.style.display = "block";

    list_title_text.innerHTML = "Review:";
});

updateconfig.addEventListener("click", function () {
    setConfig();
});

function getConfig() {
    httpGetAsync("/api/v1/getconfig", null, (res) => {
        console.log(res);

        const data = JSON.parse(res);

        prefix.value = data.prefix;
        managerreview.value = data.managerreview;
        reviewchannel.value = data.reviewchannel;
        embedcolor.value = data.embedcolor;

        permissionrole.value = data.permissionrole;
        managerrole.value = data.managerrole;
        updateconfig.value = data.updateconfig;

        qotdColor = data.embedcolor;
    });
}


function setConfig() {
    const body = `
{
    "prefix": "${prefix.value}",
    "managerreview": "${managerreview.value}",
    "reviewchannel": "${reviewchannel.value}",
    "embedcolor": "${embedcolor.value}",
    
    "permissionrole": "${permissionrole.value}",
    "managerrole": "${managerrole.value}"
}
    `;

    console.log(body);
    httpPostAsync(`/api/v1/setconfig`, body, (res) => {
        window.location.reload();
    });
}

function getQueue() {
    httpGetAsync(`/api/v1/getqueue`, null, (res) => {
        console.log(res);

        const data = JSON.parse(res);

        question_queue.innerHTML = "";

        for (let i in data.queue) {

            const q = data.queue[i];

            const date = new Date(q.time);

            const ques = (q.poll ? "Poll: " : "Question: ") + q.question;

            var card = "Error";

            card = `
        
<div class="question" style="border-left: 5px solid ${qotdColor}">
    <div class="main">
        <div class="header">
            <h3><b>Added by: ${q.user}</b></h3>
        </div>
        <div class="title">
            <h2><b>${ques}</b></h2>
        </div>
        <div class="description">
            <h4>Footer: <i>${q.footer}</i></h4>
        </div>
        <div class="footer">
            <h4>Added on: ${formatDate(date)}</h4>
        </div>
    </div>

    <div class="aside">
        <button class="delete" title="Remove" onclick="deleteQOTD("${q.uuid}")">&#128465;&#65039;</button>
    </div>
</div>

            `;

            question_queue.innerHTML += card;
        }
    });
}

function httpGetAsync(url, body, callback) {
    console.log(url);

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            callback(xmlHttp.responseText);
        }
    }

    xmlHttp.open("GET", url, true);
    xmlHttp.setRequestHeader('Content-Type', 'application/json');
    xmlHttp.send(body);
}


function httpPostAsync(url, body, callback) {
    console.log(url);

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            callback(xmlHttp.responseText);
        }
    }

    xmlHttp.open("POST", url, true);
    xmlHttp.setRequestHeader('Content-Type', 'application/json');
    xmlHttp.send(body);
}

function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`;
}

getConfig();
queue.click();