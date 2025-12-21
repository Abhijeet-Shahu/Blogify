const listBlog = document.getElementById("blogContainer");

const blogTitle = document.getElementById("blogTitle");
const blogContent = document.getElementById("blogContent");
const blogCategory = document.getElementById("blogCategory");
const publishBtn = document.getElementById("publishBtn");
const blogImage = document.getElementById("blogImageFile");

const aiBtn = document.getElementById("AI");

const API_BASE = "http://localhost:8080/api";

// ✅ Create & Save Blog
async function saveBlog() {
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));

    if (!token || !user) {
        alert("You must be logged in to publish a blog.");
        window.location.href = "login.html";
        return;
    }

    const title = blogTitle.value.trim();
    const content = blogContent.value.trim();
    const category = blogCategory.value.trim() || "General";

    if (!title || !content) {
        alert("Please add both a title and content to publish your blog.");
        return;
    }

    let imageUrl = "/assets/Logo.png";

    // ✅ If image file uploaded
    if (blogImage.files.length > 0) {
        const reader = new FileReader();
        reader.onload = async (e) => {
            imageUrl = e.target.result;
            await postBlog(title, content, category, imageUrl, token);
        };
        reader.readAsDataURL(blogImage.files[0]);
    } else {
        try {
            // ✅ Auto-generate image with AI
            const res = await fetch(`${API_BASE}/ai/generate-image`, {
                method: "POST",
                headers: { 
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}` 
                },
                body: JSON.stringify({ title, category })
            });

            if (res.ok) {
                const data = await res.json();
                imageUrl = data.imageUrl || imageUrl;
            } else {
                console.warn("AI image failed, fallback to default");
            }
        } catch (err) {
            console.error("Failed to generate image:", err);
        }

        await postBlog(title, content, category, imageUrl, token);
    }
}

// ✅ Send Blog to Backend
async function postBlog(title, content, category, imageUrl, token) {
    try {
        const res = await fetch(`${API_BASE}/blogs`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ title, content, category, blogImage: imageUrl })
        });

        if (res.ok) {
            alert("Blog Published!");
            window.location.href = "index.html";
        } else {
            const err = await res.json();
            alert("Error: " + (err.message || "Failed to publish blog"));
        }
    } catch (e) {
        console.error("Error publishing blog:", e);
        alert("Something went wrong!");
    }
}

// ✅ Auto-load blogs (if listing page)
async function publishBlog() {
    try {
        const res = await fetch(`${API_BASE}/blogs`);
        const blogs = await res.json();

        blogs.forEach(blog => {
            const article = document.createElement("article");
            const snippet = blog.content.length > 130 ? blog.content.substring(0, 130) + "..." : blog.content;

            article.className =
                "bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1 mb-6";
            article.innerHTML = `
                <img src="${blog.blogImage}" alt="Blog Image" class="w-full h-48 object-cover">
                <div class="p-4 sm:p-6">
                    <span class="inline-block bg-blue-100 text-blue-800 text-xs sm:text-sm font-medium px-2 py-1 rounded-full mb-2">${blog.category}</span>
                    <h3 class="text-lg sm:text-xl font-semibold text-gray-900 mb-1 hover:text-blue-600 transition-colors cursor-pointer">${blog.title}</h3>
                    <p class="text-gray-600 text-sm sm:text-base leading-relaxed mb-3">${snippet}</p>
                    <div class="flex items-center justify-between text-xs sm:text-sm">
                        <span class="text-gray-500">${new Date(blog.date).toLocaleDateString()}</span>
                        <button class="text-blue-600 hover:text-blue-700 font-medium">Read More →</button>
                    </div>
                </div>`;
            listBlog.appendChild(article);
        });
    } catch (err) {
        console.error("Failed to fetch blogs:", err);
    }
}

if (publishBtn) publishBtn.addEventListener("click", saveBlog);
if (listBlog) publishBlog();

// ✅ AI Content Generation (unchanged)
async function generateContentWithAi() {
    const title = blogTitle.value.trim();
    const category = blogCategory.value.trim();

    if (!title || !category) {
        alert("Please enter a blog title with category.");
        return;
    }

    aiBtn.disabled = true;
    aiBtn.textContent = "Generating...";

    try {
        const response = await fetch(`${API_BASE}/ai/generate`, {
            method: "POST",
            headers: { 
                "Content-Type": "application/json" 
            },
            body: JSON.stringify({ title, category })
        });

        const data = await response.json();
        if (data.content) {
            blogContent.value = data.content;
        } else {
            alert("Failed to generate content. Please try again.");
        }
    } catch (error) {
        console.error("Error generating content:", error);
        alert("An error occurred while generating content. Please try again.");
    }

    aiBtn.disabled = false;
    aiBtn.textContent = "Generate with AI";
}

if (aiBtn) aiBtn.addEventListener("click", generateContentWithAi);
