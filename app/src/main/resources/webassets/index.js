const queue = document.getElementById('queue');
const review = document.getElementById('review');

const question_queue = document.getElementById('question-queue');
const question_review = document.getElementById('question-review');

function deleteQOTD(index) {

}

queue.addEventListener("click", function () {
    question_queue.style.display = "block";
    question_review.style.display = "none";
});

review.addEventListener("click", function () {
    question_queue.style.display = "none";
    question_review.style.display = "block";
});

queue.click();