document.addEventListener("DOMContentLoaded" , () =>{
    const commentForm = document.getElementById("commentForm");
    const commentcontent = document.getElementById("commentinput");
    const commentcontainer = document.getElementById("commentContainer");

    let comments = JSON.parse(localStorage.getItem("comments")) || [];


    function renderComments(){
        commentcontainer.innerHTML = "";
        comments.forEach(comment => {
            const div = document.createElement("div");
            div.className = "p-4 bg-gray-50 rounded-lg";

            div.innerHTML = `
                <p class="font-semibold text-gray-800">${comment.author}</p>
                <p class="text-gray-600 text-sm sm:text-base">${comment.content}</p>
            `;

            commentcontainer.appendChild(div);
            
        });
    }

    commentForm.addEventListener("submit", (e) =>{
        e.preventDefault();

        const author = "Shadow";
        const content = commentcontent.value.trim();

        if (!author || !content){
            return;
        }
        const newComment = {
            author,
            content
        };
        comments.unshift(newComment);

        localStorage.setItem("comments", JSON.stringify(comments));

        renderComments();

        commentForm.reset();
    });

    renderComments();
});  