const apiUrl = "http://semantle.ru:8080/v0/api"

let langRequest = new XMLHttpRequest();

langRequest.open('GET', apiUrl + "/getLanguages", false);

let languages = {};

langRequest.onload = function() {
    languages = JSON.parse(langRequest.responseText);
}

langRequest.send();

const urlParams = new URLSearchParams(window.location.search);
const solutionId = urlParams.get('q');

let request = new XMLHttpRequest();

request.open('GET', apiUrl + "/getSolution?id=" + solutionId);

request.onload = function() {
    let answer = JSON.parse(request.responseText);
    document.getElementById('code_container').innerText = answer.code;
    if (answer.name != null) {
        document.getElementById('name').innerText = answer.name;
    } else {
        document.getElementById('name_container').style.display = "none";
    }
    if (answer.description != null) {
        document.getElementById('description').innerText = answer.description;
    } else {
        document.getElementById('description_container').style.display = "none";
    }
    document.getElementById('language').innerText = languages[answer.languageId].name;
}

request.send();

interferenceRequest = new XMLHttpRequest();

interferenceRequest.open('GET', apiUrl + '/getInterferences?solution_id=' + solutionId);

interferenceRequest.onload = function () {
    let answer = JSON.parse(interferenceRequest.responseText);
    let interferenceContainer = document.getElementById('interference_container');
    answer.forEach(element => {
        let container = document.createElement('div');
        container.setAttribute('class', 'interference');
        let link = document.createElement('a');
        link.setAttribute('href', 'viewResult.html?q=' + element.interferedSolutionId);
        link.innerText = "#" + element.interferedSolutionId;
        container.appendChild(link);
        let interference =  document.createElement('span');
        interference.classList.add('interferenceFraction');
        interference.innerText = Math.round(element.interferenceFraction * 100) + "%";
        container.appendChild(interference);
        interferenceContainer.appendChild(container);

    });
}

interferenceRequest.send();

