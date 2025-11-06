document.addEventListener("DOMContentLoaded", () => {
    // Handle redirect clicks (Page 1)
    const redirectButtons = document.querySelectorAll(".redirect-btn");

    redirectButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            const postId = btn.getAttribute("data-post"); // link button to a title
            const postTitle = document.getElementById(postId);

            if (postTitle) {
                localStorage.setItem("sharedText", postTitle.innerHTML);
                window.location.href = "ReadPage.html";
            }
        });
    });

    // Handle displaying saved text (Page 2)
    const titlePost = document.getElementById("titlePost");
    if (titlePost) {
        const savedText = localStorage.getItem("sharedText");
        if (savedText) {
            titlePost.innerHTML = savedText;
        }
    }

    const redirectSection = document.querySelectorAll(".redirect-section");

    redirectSection.forEach((btn) =>{
        btn.addEventListener("click",()=>{
            let postSection = btn.getAttribute("data-post");
            let sectionlogo = btn.getAttribute("data-post1");
            let contnet = btn.getAttribute("data-post2");

            let postselectionlogo = document.getElementById(sectionlogo);
            let postSectionText = document.getElementById(postSection);
            let postcontent = document.getElementById(contnet);

            if(postSectionText && postselectionlogo && contnet){
                localStorage.setItem("sharedlogo",postselectionlogo.outerHTML);
                localStorage.setItem("sharedText", postSectionText.innerHTML);
                localStorage.setItem("sharedcontent", postcontent.innerHTML);
                window.location.href = "section.html";
            }
        });
    });

    const sectionTextPost = document.getElementById("TitleText");
    const secetionlogo = document.getElementById("receivelogo");
    const content = document.getElementById("showcontent");

    if(sectionTextPost && secetionlogo){    
        const savedlogo = localStorage.getItem("sharedlogo");
        const savedTitle = localStorage.getItem("sharedText");
        const savedcontent = localStorage.getItem("sharedcontent");

        if(savedTitle && savedlogo && savedcontent){
            content.innerHTML = savedcontent;
            sectionTextPost.innerHTML = savedTitle;
            secetionlogo.outerHTML = `<div class="flex items-center justify-center mb-6" id="receivelogo">${savedlogo}</div>`;
        }
    }


});
