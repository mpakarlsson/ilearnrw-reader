function showToast(toast){
	ReaderInterface.showToast(toast);
}

function getBodyContent(str) {
	var bodyHTML = document.body.innerHTML;
	ReaderInterface.splitSentencesSpeak(bodyHTML, 0, 'button');
}

function scrollToElement(id){
	var elem = document.getElementById(id);
	var x = 0;
	var y = 0;
	
	while(elem != null) {
		x += elem.offsetLeft;
		y += elem.offsetTop;
		elem = elem.offsetParent
	}
	window.scrollTo(x,y);
}

function isElementTopLeftInViewport(id){
	var elem = document.getElementById(id);
	var rect = elem.getBoundingClientRect();
	var isVisible = (
		rect.top >= 0 && rect.top <= (window.innerHeight || document.documentElement.clientHeight) &&
		rect.left >= 0 && rect.left <= (window.innerWidth || document.documentElement.clientWidth)
	);
	return ReaderInterface.isElementInViewport(id, isVisible);
}

function isElementInViewport(id) {
	var elem = document.getElementById(id);
	var rect = elem.getBoundingClientRect();

	var isVisible = (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && 
        rect.right <= (window.innerWidth || document.documentElement.clientWidth) 
    );
    return ReaderInterface.isElementInViewport(id, isVisible);
}

function highlight(id, color){
	var element = document.getElementById(id);
	element.style.backgroundColor=color;
	return true;
}

function removeHighlight(id){
	var element = document.getElementById(id);
	element.style.backgroundColor='transparent';
	return true;
}

function highlightPart(id, start, end, color){
	var node =  document.getElementById(id);
	var range = document.createRange();
	range.setStart(node.firstChild, start);
	range.setEnd(node.firstChild, end);
	
	var span = document.createElement('span');
	span.className = id;
	span.style.backgroundColor = color;
	
	range.surroundContents(span);
	range.detach();
	ReaderInterface.saveHighlightInformation(id, start, end);
	
	return true;
};

function unhighlight(id, start, end){
	var spans = document.getElementsByTagName('span');
	for(var i=0; i<spans.length; i++){
		if(spans[i].className == id){
			var container = spans[i].parentNode;
			var node = spans[i].firstChild;
			container.insertBefore(node, spans[i]);
			container.removeChild(spans[i]);
			ReaderInterface.removeHighlightInformation(id);
		}
	} 
};

function getSentences(){
	var sents = document.getElementsByTagName('**SENTENCE_TAG**');
	var result = '';
	for(var i=0; i<sents.length; i++){
		if(i+1==sents.length){
			result += sents[i].id;
		} else {
			result += sents[i].id + ',';
		}
		sents[i].onclick = function() {
			var body = document.body.innerHTML;
			var index = body.indexOf(this.id);
			var part = body.substring(0, index);
			var lastIndex = part.lastIndexOf('<');
			part = part.substring(lastIndex);
			body = body.substring(body.indexOf(this.id));
			body = part + body;
			ReaderInterface.clickSentence(body, this.id);
		};
	};
	ReaderInterface.getSentences(result);
}

function getWords(){
	var words = document.getElementsByTagName('**WORD_TAG**');
	var result = '';
	for(var i=0; i<words.length; i++){
		words[i].ontouchstart = function(){
			ReaderInterface.touchWord(this.id);
		};
		
		if(i+1==words.length){
			result += words[i].id;
		} else {
			result += words[i].id + ',';
		}
			
		words[i].onclick = function() {
			var body = document.body.innerHTML;
			var index = body.indexOf(this.id);
			var part = body.substring(0, index);
			var lastIndex = part.lastIndexOf('<');
			part = part.substring(lastIndex);
			body = body.substring(body.indexOf(this.id));
			body = part + body;
			ReaderInterface.clickWord(body, this.id); 
		};
	}
	ReaderInterface.getWords(result);
};

function updateCurrentPosition(id, checkParent){
	var node =  document.getElementById(id);
	var other = '';
	if(checkParent==1) {
		other = node.parentNode.id;
	} else {
		other = node.childNodes[0].id;
	}
	ReaderInterface.updateCurrentPosition(other, checkParent);	
};

function speakSentence(id){
	var element = document.getElementById(id);
	ReaderInterface.speakSentence(element.innerText);
};

function longClick(id) {
	var word = document.getElementById(id);
	ReaderInterface.longClick(word.innerHTML);
};