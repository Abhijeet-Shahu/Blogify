(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        const form = document.getElementById("blogForm");
        if (!form) return;

        const title = document.getElementById("blogTitle");
        const content = document.getElementById("blogContent");
        const status = document.getElementById("blogStatus");
        const preview = document.getElementById("previewView");
        const editor = document.getElementById("editorView");
        const previewButton = document.getElementById("previewBtn");
        const imageInput = form.querySelector('input[name="featuredImage"]');
        const imagePreview = document.getElementById("imagePreview");
        const notification = document.getElementById("aiNotification");
        const category = form.querySelector('select[name="categoryId"]');

        function updateStats() {
            const words = content.value.trim() ? content.value.trim().split(/\s+/).length : 0;
            document.getElementById("wordCount").textContent = words;
            document.getElementById("charCount").textContent = content.value.length;
            document.getElementById("readTime").textContent = Math.max(1, Math.ceil(words / 200)) + " min";
        }

        function togglePreview() {
            const showingPreview = preview.classList.contains("hidden");
            preview.classList.toggle("hidden", !showingPreview);
            editor.classList.toggle("hidden", showingPreview);
            document.getElementById("previewTitle").textContent = title.value || "Your story title";
            document.getElementById("previewContent").textContent = content.value || "Your story content will appear here.";
            previewButton.textContent = showingPreview ? "Edit" : "Preview";
        }

        function submitWithStatus(nextStatus) {
            status.value = nextStatus;
            form.requestSubmit();
        }

        function showNotification(message, type) {
            notification.textContent = message;
            notification.className = "fixed right-4 top-4 z-50 rounded-xl border p-4 text-sm " +
                (type === "error" ? "border-[var(--blogify-danger)] text-[var(--blogify-danger)]" : "border-[var(--blogify-accent-strong)]");
        }

        async function generateContent() {
            if (!title.value.trim() || !category.value) {
                showNotification("Add a title and choose a category first.", "error");
                return;
            }
            const token = form.querySelector('input[name="_csrf"]')?.value || "";
            const categoryName = category.options[category.selectedIndex].text;
            const button = document.getElementById("AI");
            button.disabled = true;
            button.textContent = "Generating...";
            try {
                const response = await fetch("/api/ai/generate", {
                    method: "POST",
                    headers: {"Content-Type": "application/json", "X-CSRF-TOKEN": token},
                    body: JSON.stringify({title: title.value.trim(), category: categoryName})
                });
                if (!response.ok) throw new Error("AI request failed");
                const result = await response.json();
                content.value = result.content || "";
                updateStats();
                showNotification("Draft content generated.", "success");
            } catch (error) {
                showNotification("AI assistance is currently unavailable.", "error");
            } finally {
                button.disabled = false;
                button.textContent = "Generate with AI";
            }
        }

        title.addEventListener("input", updateStats);
        content.addEventListener("input", updateStats);
        previewButton.addEventListener("click", togglePreview);
        document.getElementById("saveBtn").addEventListener("click", () => submitWithStatus("DRAFT"));
        document.getElementById("publishBtn").addEventListener("click", () => {
            if (!title.value.trim() || !content.value.trim()) {
                showNotification("A title and content are required to publish.", "error");
                return;
            }
            submitWithStatus("PUBLISHED");
        });
        document.getElementById("AI").addEventListener("click", generateContent);
        imageInput?.addEventListener("change", function () {
            const file = imageInput.files[0];
            if (!file || !imagePreview) return;
            imagePreview.src = URL.createObjectURL(file);
            imagePreview.classList.remove("hidden");
        });
        updateStats();
    });
})();
