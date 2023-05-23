const config = document.getElementsByClassName('qotd-config')[0];

const modal = document.getElementById('modal');
const nauthor = document.getElementById('nauthor');
const nquestion = document.getElementById('nquestion');
const nfooter = document.getElementById('nfooter');
const ntype = document.getElementById('nquestiontype');

const queue = document.getElementById('queue');
const review = document.getElementById('review');

const question_queue = document.getElementById('question-queue');
const question_review = document.getElementById('question-review');

const prefix = document.getElementById('prefix');
const qotdchannel = document.getElementById('qotdchannel');
const managerreview = document.getElementById('managerreview');
const reviewchannel = document.getElementById('reviewchannel');
const embedcolor = document.getElementById('embedcolor');
const paused = document.getElementById('paused');

const permissionrole = document.getElementById('permissionrole');
const managerrole = document.getElementById('managerrole');


const updateconfig = document.getElementById('updateconfig');


const list_title_text = document.getElementById('list-title-text');

var qotdColor = "#000";

function deleteQOTD(type, uuid) {
    httpPostAsync(`/api/v1/delete`, `{"type":"${type}", "uuid":"${uuid}"}`, (res) => {
        window.location = window.location.origin + (type === 'review' ? "?tab=review" : "");
    });
}

function approveQOTD(uuid) {
    httpPostAsync(`/api/v1/approve`, `{"uuid":"${uuid}"}`, (res) => {
        window.location = window.location.origin + "?tab=review";
    });
}

function postNext() {
    httpGetAsync("/api/v1/postnext", null, (res) => {
        alert("Next QOTD Posted!");
        window.location.reload();
    });
}

function showModal() {
    modal.classList.add('fade-in');
    modal.style.display = 'block';
}

function hideModal() {
    modal.classList.add('fade-out');
    setTimeout(function () {
        modal.classList.remove('fade-in');
        modal.classList.remove('fade-out');
        modal.style.display = 'none';
    }, 100);
}

function submitPost() {
    const body = `
    {
        "author": ${nauthor},
        "question": ${nquestion},
        "footer": ${nfooter},
        "type": ${ntype}
    }
    `;

    console.log(body);
    httpPostAsync(`/api/v1/newpost`, body, (res) => {
        alert("New QOTD Added!");
        window.location.reload();
    });
}

modal.addEventListener("keypress", function (e) {
    if (e.key === 'Escape') {
        hideModal();
    }
});

modal.addEventListener("click", function (e) {
    if (e.target === e.currentTarget) {
        hideModal();
    }
});

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

    getReview();
});

updateconfig.addEventListener("click", function () {
    setConfig();
});

function getConfig() {
    httpGetAsync("/api/v1/getconfig", null, (res) => {
        console.log(res);

        const data = JSON.parse(res);

        prefix.value = data.prefix;
        qotdchannel.value = data.qotdchannel;
        managerreview.value = data.managerreview;
        reviewchannel.value = data.reviewchannel;
        embedcolor.value = data.embedcolor;
        trivia.value = data.trivia;
        paused.value = data.paused;

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
            "qotdchannel": "${qotdchannel.value}",
            "managerreview": "${managerreview.value}",
            "reviewchannel": "${reviewchannel.value}",
            "embedcolor": "${embedcolor.value}",
            "trivia": "${trivia.value}",
            "paused": "${paused.value}",

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

        if (data.queue.length == 0) {
            question_queue.innerHTML = "Nothing to see here.";
        }

        for (let i in data.queue) {

            const q = data.queue[i];

            const date = new Date(q.time);

            const ques = (q.poll ? "Poll: " : "Question: ") + q.question;

            var card = `

<div class="question" style = "border-left: 5px solid ${qotdColor}">
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
        <button class="delete" title="Remove" onclick="deleteQOTD('queue','${q.uuid}')">&#128465;&#65039;</button>
    </div>
</div >

            `;

            question_queue.innerHTML += card;
        }
    });
}

function getReview() {
    httpGetAsync(`/api/v1/getreview`, null, (res) => {
        console.log(res);

        const data = JSON.parse(res);

        question_review.innerHTML = "";

        if (data.review.length == 0) {
            question_review.innerHTML = "Nothing to see here.";
        }

        for (let i in data.review) {

            const q = data.review[i];

            const date = new Date(q.time);

            const ques = (q.poll ? "Poll: " : "Question: ") + q.question;

            var card = `

<div class="question" style = "border-left: 5px solid ${qotdColor}">
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
        <button class="deny" id="deny" title="Deny" onclick="deleteQOTD('review','${q.uuid}')">&#10060;</button>
        <button class="approve" id="approve" title="Approve" onclick="approveQOTD('${q.uuid}')">&#9989;</button>
    </div>
</div >

            `;

            question_review.innerHTML += card;
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

    return `${year} /${month}/${day} ${hours}:${minutes}:${seconds} `;
}

hideModal();
getConfig();

const urlParams = new URLSearchParams(window.location.search);
const selectedTab = urlParams.get('tab');

console.log(selectedTab);

if (selectedTab === "review") {
    review.click();
} else {
    queue.click();
}