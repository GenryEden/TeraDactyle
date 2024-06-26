const apiUrl = "http://semantle.ru:8080/v0/api"

let langRequest = new XMLHttpRequest();

langRequest.open('GET', apiUrl + "/getLanguages", false);

langRequest.onload = function() {
    let data = JSON.parse(langRequest.responseText);
    let select = document.getElementById('language');
    data.forEach(element => {
        console.log(element)
        let option = document.createElement('option');
        option.setAttribute('value', element.id);
        option.innerText = element.name;
        select.appendChild(option);
    });
}

langRequest.send();

function waitAnim() {

}

let form = document.getElementById('send_form');
let filePick = document.getElementById('filePick');
form.onsubmit = function(event) {
    waitAnim();
    var reader = new FileReader();
    let requestBody = {};
    requestBody.name = document.getElementById('name').value;
    requestBody.description = document.getElementById('description').value;
    requestBody.languageId = document.getElementById('language').value;
    event.preventDefault();
    reader.onload = function (e) {
        requestBody.code = e.target.result;
        let request = new XMLHttpRequest();
        request.open('POST', apiUrl + '/putSolution')
        request.setRequestHeader('Content-Type', 'application/json');
        request.onload = function() {
            window.location = "viewResult.html?q=" + request.responseText;
        }
        request.send(JSON.stringify(requestBody));
    }
    reader.readAsText(filePick.files[0])

}